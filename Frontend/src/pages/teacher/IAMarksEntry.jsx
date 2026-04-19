import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMySubjects, submitIAMarks, getStudentsByClass } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

const EMPTY_STUDENTS = [];

export default function IAMarksEntry() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("IA Marks Entry"), [setPageTitle]);
  const queryClient = useQueryClient();

  const [selectedSubject, setSelectedSubject] = useState("");
  const [iaRound, setIaRound] = useState("IA-1");
  const [maxMarks, setMaxMarks] = useState(30);
  const [entries, setEntries] = useState([]);

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

  // Fetch students for the selected class
  const { data: students = EMPTY_STUDENTS } = useQuery({
    queryKey: ["teacher", "students-by-class", classId],
    queryFn: async () => {
      const { data } = await getStudentsByClass(classId);
      return data;
    },
    enabled: !!classId,
  });

  // Populate entries when students load or subject changes
  useEffect(() => {
    if (students.length > 0 && classId) {
      setEntries(
        students.map((s) => ({
          studentId: s.userId,
          studentName: s.fullName,
          rollNumber: s.rollNumber,
          obtainedMarks: "",
          absent: false,
        }))
      );
    } else {
      setEntries([]);
    }
  }, [students, classId]);

  const mutation = useMutation({
    mutationFn: async (data) => {
      const response = await submitIAMarks(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("IA marks submitted successfully!");
      setEntries([]);
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to submit marks"),
  });

  const handleSubmit = () => {
    if (!subjectId || !classId) {
      toast.error("Please select a subject");
      return;
    }
    mutation.mutate({
      subjectId,
      classId,
      iaRound,
      maxMarks: parseFloat(maxMarks),
      entries: entries.map((e) => ({
        studentId: e.studentId,
        obtainedMarks: e.absent ? 0 : parseFloat(e.obtainedMarks || 0),
        absent: e.absent || false,
      })),
    });
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">IA Marks Entry</h2>

      <Card>
        <div className="grid md:grid-cols-3 gap-4">
          <Select
            label="Subject & Class"
            placeholder="Select subject..."
            options={subjectOptions}
            value={selectedSubject}
            onValueChange={setSelectedSubject}
          />
          <Select
            label="IA Round"
            options={[
              { value: "IA-1", label: "IA-1" },
              { value: "IA-2", label: "IA-2" },
              { value: "IA-3", label: "IA-3" },
            ]}
            value={iaRound}
            onValueChange={setIaRound}
          />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Max Marks</label>
            <input
              type="number"
              min="1"
              value={maxMarks}
              onChange={(e) => setMaxMarks(e.target.value)}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
        </div>
      </Card>

      {selectedSubject && entries.length > 0 && (
        <Card>
          <CardTitle>Enter Marks</CardTitle>
          <div className="overflow-x-auto mt-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Student</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                  <th className="text-center px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Marks</th>
                  <th className="text-center px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Absent</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry, idx) => (
                  <tr key={entry.studentId} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 text-text-primary">{entry.studentName}</td>
                    <td className="px-4 py-3 text-text-secondary">{entry.rollNumber}</td>
                    <td className="px-4 py-3 text-center">
                      <input
                        type="number"
                        min="0"
                        max={maxMarks}
                        value={entry.absent ? 0 : entry.obtainedMarks || ""}
                        disabled={entry.absent}
                        onChange={(e) => {
                          const updated = [...entries];
                          updated[idx] = { ...updated[idx], obtainedMarks: e.target.value };
                          setEntries(updated);
                        }}
                        className="w-20 bg-bg-primary border border-border-light rounded-lg px-3 py-1.5 text-sm text-center focus:outline-none focus:ring-2 focus:ring-accent-primary/30 disabled:opacity-50"
                      />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <input
                        type="checkbox"
                        checked={entry.absent || false}
                        onChange={(e) => {
                          const updated = [...entries];
                          updated[idx] = {
                            ...updated[idx],
                            absent: e.target.checked,
                            obtainedMarks: e.target.checked ? 0 : updated[idx].obtainedMarks,
                          };
                          setEntries(updated);
                        }}
                        className="rounded"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="mt-4">
            <Button onClick={handleSubmit} loading={mutation.isPending}>
              Submit Marks
            </Button>
          </div>
        </Card>
      )}

      {selectedSubject && entries.length === 0 && (
        <Card>
          <p className="text-sm text-text-secondary text-center py-8">
            Student list will be populated from the backend when data exists for this subject/class.
          </p>
        </Card>
      )}
    </div>
  );
}
