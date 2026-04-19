import { useEffect, useMemo, useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Flag, Search } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMySubjects, flagStudent as flagStudentApi, getStudentsByClass } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";

const EMPTY_STUDENTS = [];

export default function FlagStudent() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Flag Student"), [setPageTitle]);

  const [selectedSubject, setSelectedSubject] = useState("");
  const [studentId, setStudentId] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [note, setNote] = useState("");

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

  const filteredStudents = useMemo(() => {
    if (!searchTerm) return students;
    const term = searchTerm.toLowerCase();
    return students.filter(
      (s) =>
        s.fullName?.toLowerCase().includes(term) ||
        s.rollNumber?.toLowerCase().includes(term)
    );
  }, [students, searchTerm]);

  const selectedStudent = students.find((s) => s.userId === studentId);

  const mutation = useMutation({
    mutationFn: async (data) => {
      const response = await flagStudentApi(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Student flagged successfully!");
      setStudentId("");
      setSearchTerm("");
      setNote("");
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to flag student"),
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!subjectId || !studentId) {
      toast.error("Please select a subject and a student");
      return;
    }
    mutation.mutate({
      studentId,
      subjectId,
      note: note || undefined,
    });
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Flag Student</h2>

      <Card>
        <CardTitle className="flex items-center gap-2">
          <Flag className="w-4 h-4 text-status-warning" />
          Raise a Flag
        </CardTitle>
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <Select
            label="Subject & Class"
            placeholder="Select subject..."
            options={subjectOptions}
            value={selectedSubject}
            onValueChange={(v) => {
              setSelectedSubject(v);
              setStudentId("");
              setSearchTerm("");
            }}
          />

          {/* Searchable student picker */}
          {selectedSubject && (
            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-text-primary">Student</label>
              {selectedStudent ? (
                <div className="flex items-center justify-between bg-bg-secondary border border-border-light rounded-xl px-4 py-2.5">
                  <span className="text-sm text-text-primary">
                    {selectedStudent.rollNumber} — {selectedStudent.fullName}
                  </span>
                  <button
                    type="button"
                    onClick={() => { setStudentId(""); setSearchTerm(""); }}
                    className="text-xs text-accent-primary hover:underline"
                  >
                    Change
                  </button>
                </div>
              ) : (
                <>
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-tertiary" />
                    <input
                      type="text"
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      placeholder="Search by name or roll number..."
                      className="w-full bg-bg-primary border border-border-light rounded-xl pl-9 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30 placeholder:text-text-tertiary"
                    />
                  </div>
                  {students.length > 0 && (
                    <div className="max-h-48 overflow-y-auto border border-border-light rounded-xl bg-bg-secondary divide-y divide-border-light">
                      {filteredStudents.length === 0 ? (
                        <p className="text-xs text-text-tertiary text-center py-3">No matching students</p>
                      ) : (
                        filteredStudents.map((s) => (
                          <button
                            key={s.userId}
                            type="button"
                            onClick={() => { setStudentId(s.userId); setSearchTerm(""); }}
                            className="w-full text-left px-4 py-2 hover:bg-bg-hover text-sm transition-colors"
                          >
                            <span className="font-medium text-text-primary">{s.rollNumber}</span>
                            <span className="text-text-secondary ml-2">{s.fullName}</span>
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Note (optional)</label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Any additional notes about the concern..."
              rows={3}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30 placeholder:text-text-tertiary resize-none"
            />
          </div>
          <Button type="submit" loading={mutation.isPending}>
            Flag Student
          </Button>
        </form>
      </Card>
    </div>
  );
}
