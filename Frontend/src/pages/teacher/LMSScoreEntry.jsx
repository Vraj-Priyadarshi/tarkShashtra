import { useEffect, useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMySubjects, submitLMSScores, getStudentsByClass } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";

const EMPTY_STUDENTS = [];

export default function LMSScoreEntry() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("LMS Score Entry"), [setPageTitle]);

  const [selectedSubject, setSelectedSubject] = useState("");
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

  // Populate entries when students load
  useEffect(() => {
    if (students.length > 0 && classId) {
      setEntries(
        students.map((s) => ({
          studentId: s.userId,
          studentName: s.fullName,
          rollNumber: s.rollNumber,
          score: "",
        }))
      );
    } else {
      setEntries([]);
    }
  }, [students, classId]);

  const mutation = useMutation({
    mutationFn: async (data) => {
      const response = await submitLMSScores(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("LMS scores submitted!");
      setEntries([]);
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to submit scores"),
  });

  const handleSubmit = () => {
    if (!subjectId || !classId) {
      toast.error("Please select a subject");
      return;
    }
    mutation.mutate({
      subjectId,
      classId,
      entries: entries.map((e) => ({
        studentId: e.studentId,
        score: parseFloat(e.score || 0),
      })),
    });
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">LMS Score Entry</h2>

      <Card>
        <Select
          label="Subject & Class"
          placeholder="Select subject..."
          options={subjectOptions}
          value={selectedSubject}
          onValueChange={setSelectedSubject}
        />
      </Card>

      {selectedSubject && entries.length > 0 && (
        <Card>
          <CardTitle>Enter LMS Scores</CardTitle>
          <div className="overflow-x-auto mt-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Student</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                  <th className="text-center px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Score (0-100)</th>
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
                        max="100"
                        value={entry.score || ""}
                        onChange={(e) => {
                          const updated = [...entries];
                          updated[idx] = { ...updated[idx], score: e.target.value };
                          setEntries(updated);
                        }}
                        className="w-20 bg-bg-primary border border-border-light rounded-lg px-3 py-1.5 text-sm text-center focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
                        placeholder="0-100"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="mt-4">
            <Button onClick={handleSubmit} loading={mutation.isPending}>
              Submit Scores
            </Button>
          </div>
        </Card>
      )}

      {selectedSubject && entries.length === 0 && (
        <Card>
          <p className="text-sm text-text-secondary text-center py-8">
            Student list will be loaded from the backend for the selected subject/class.
          </p>
        </Card>
      )}
    </div>
  );
}
