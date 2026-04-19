import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMySubjects, createAttendanceSession, getAttendanceHistory, getStudentsByClass } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import { formatDate } from "../../lib/utils";

const EMPTY_STUDENTS = [];

export default function AttendanceEntry() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Attendance Entry"), [setPageTitle]);

  const queryClient = useQueryClient();
  const [selectedSubject, setSelectedSubject] = useState("");
  const [sessionDate, setSessionDate] = useState(new Date().toISOString().split("T")[0]);
  const [entryMode, setEntryMode] = useState("PER_SESSION");
  const [records, setRecords] = useState([]);

  const { data: subjects = [] } = useQuery({
    queryKey: ["teacher", "my-subjects"],
    queryFn: async () => {
      const { data } = await getMySubjects("2025-26");
      return data;
    },
  });

  const subjectOptions = subjects.map((s) => ({
    value: `${s.subject?.id || s.subjectId}__${s.classEntity?.id || s.classId}`,
    label: `${s.subject?.name || s.subjectName} - ${s.classEntity?.name || s.className}`,
  }));

  const [subjectId, classId] = selectedSubject.split("__");

  // Get attendance history
  const { data: history = [] } = useQuery({
    queryKey: ["teacher", "attendance-history", subjectId, classId],
    queryFn: async () => {
      const { data } = await getAttendanceHistory(subjectId, classId);
      return data;
    },
    enabled: !!subjectId && !!classId,
  });

  // Fetch students for the selected class
  const { data: students = EMPTY_STUDENTS } = useQuery({
    queryKey: ["teacher", "students-by-class", classId],
    queryFn: async () => {
      const { data } = await getStudentsByClass(classId);
      return data;
    },
    enabled: !!classId,
  });

  // Populate records when students load or subject changes
  useEffect(() => {
    if (students.length > 0 && classId) {
      setRecords(
        students.map((s) => ({
          studentId: s.userId,
          studentName: s.fullName,
          rollNumber: s.rollNumber,
          status: "PRESENT",
          percentage: "",
        }))
      );
    } else {
      setRecords([]);
    }
  }, [students, classId]);

  const mutation = useMutation({
    mutationFn: async (data) => {
      const response = await createAttendanceSession(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Attendance recorded successfully!");
      queryClient.invalidateQueries({ queryKey: ["teacher", "attendance-history"] });
      setRecords([]);
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to record attendance"),
  });

  const handleSubmit = () => {
    if (!subjectId || !classId) {
      toast.error("Please select a subject");
      return;
    }
    if (records.length === 0) {
      toast.error("No attendance records to submit");
      return;
    }
    mutation.mutate({
      subjectId,
      classId,
      sessionDate,
      entryMode,
      records,
    });
  };

  const markAll = (status) => {
    setRecords((prev) => prev.map((r) => ({ ...r, status })));
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Attendance Entry</h2>

      {/* Controls */}
      <Card>
        <div className="grid md:grid-cols-3 gap-4">
          <Select
            label="Subject & Class"
            placeholder="Select subject..."
            options={subjectOptions}
            value={selectedSubject}
            onValueChange={setSelectedSubject}
          />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Date</label>
            <input
              type="date"
              value={sessionDate}
              onChange={(e) => setSessionDate(e.target.value)}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
          <Select
            label="Entry Mode"
            options={[
              { value: "PER_SESSION", label: "Per Session (Present/Absent)" },
              { value: "BULK_PERCENTAGE", label: "Bulk Percentage" },
            ]}
            value={entryMode}
            onValueChange={setEntryMode}
          />
        </div>
      </Card>

      {/* Attendance Table */}
      {selectedSubject && records.length > 0 && (
        <Card>
          <div className="flex items-center justify-between mb-4">
            <CardTitle>Mark Attendance</CardTitle>
            {entryMode === "PER_SESSION" && (
              <div className="flex gap-2">
                <Button size="sm" variant="ghost" onClick={() => markAll("PRESENT")}>
                  All Present
                </Button>
                <Button size="sm" variant="ghost" onClick={() => markAll("ABSENT")}>
                  All Absent
                </Button>
              </div>
            )}
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Student</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                  <th className="text-center px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Status</th>
                </tr>
              </thead>
              <tbody>
                {records.map((rec, idx) => (
                  <tr key={rec.studentId} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 text-text-primary">{rec.studentName}</td>
                    <td className="px-4 py-3 text-text-secondary">{rec.rollNumber}</td>
                    <td className="px-4 py-3">
                      {entryMode === "PER_SESSION" ? (
                        <div className="flex items-center justify-center gap-4">
                          <label className="flex items-center gap-1.5 cursor-pointer">
                            <input
                              type="radio"
                              name={`status-${rec.studentId}`}
                              checked={rec.status === "PRESENT"}
                              onChange={() => {
                                const updated = [...records];
                                updated[idx] = { ...updated[idx], status: "PRESENT" };
                                setRecords(updated);
                              }}
                              className="text-status-success"
                            />
                            <span className="text-xs">Present</span>
                          </label>
                          <label className="flex items-center gap-1.5 cursor-pointer">
                            <input
                              type="radio"
                              name={`status-${rec.studentId}`}
                              checked={rec.status === "ABSENT"}
                              onChange={() => {
                                const updated = [...records];
                                updated[idx] = { ...updated[idx], status: "ABSENT" };
                                setRecords(updated);
                              }}
                              className="text-status-error"
                            />
                            <span className="text-xs">Absent</span>
                          </label>
                        </div>
                      ) : (
                        <input
                          type="number"
                          min="0"
                          max="100"
                          value={rec.percentage || ""}
                          onChange={(e) => {
                            const updated = [...records];
                            updated[idx] = { ...updated[idx], percentage: e.target.value };
                            setRecords(updated);
                          }}
                          className="w-20 mx-auto bg-bg-primary border border-border-light rounded-lg px-3 py-1.5 text-sm text-center focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
                          placeholder="%"
                        />
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-4">
            <Button onClick={handleSubmit} loading={mutation.isPending}>
              Submit Attendance
            </Button>
          </div>
        </Card>
      )}

      {selectedSubject && records.length === 0 && (
        <Card>
          <p className="text-sm text-text-secondary text-center py-8">
            Student list will load from the backend when attendance is recorded for this subject/class.
            Submit attendance data to get started.
          </p>
        </Card>
      )}

      {/* History */}
      {history.length > 0 && (
        <Card>
          <CardTitle>Attendance History</CardTitle>
          <div className="overflow-x-auto mt-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Date</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Mode</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Records</th>
                </tr>
              </thead>
              <tbody>
                {history.map((session, i) => (
                  <tr key={session.id || i} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 text-text-primary">{formatDate(session.sessionDate)}</td>
                    <td className="px-4 py-3 text-text-secondary">{session.entryMode}</td>
                    <td className="px-4 py-3 text-right text-text-secondary">
                      {session.records?.length || "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
