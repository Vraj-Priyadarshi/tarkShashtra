import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, FileText } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getMySubjects, createAssignment, getAssignments, markSubmissions, getStudentsByClass } from "../../api/teacher";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";
import Modal from "../../components/ui/Modal";
import Input from "../../components/ui/Input";
import Badge from "../../components/ui/Badge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import { formatDate } from "../../lib/utils";

export default function AssignmentManagement() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Assignment Management"), [setPageTitle]);
  const queryClient = useQueryClient();

  const [selectedSubject, setSelectedSubject] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [title, setTitle] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [selectedAssignment, setSelectedAssignment] = useState(null);
  const [submissions, setSubmissions] = useState([]);

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
  const { data: students = [] } = useQuery({
    queryKey: ["teacher", "students-by-class", classId],
    queryFn: async () => {
      const { data } = await getStudentsByClass(classId);
      return data;
    },
    enabled: !!classId,
  });

  // When an assignment is selected, populate submissions with student list
  useEffect(() => {
    if (selectedAssignment && students.length > 0) {
      setSubmissions(
        students.map((s) => ({
          studentId: s.userId,
          studentName: s.fullName,
          status: "NOT_SUBMITTED",
        }))
      );
    }
  }, [selectedAssignment, students]);

  const { data: assignments = [], isLoading } = useQuery({
    queryKey: ["teacher", "assignments", subjectId, classId],
    queryFn: async () => {
      const { data } = await getAssignments(subjectId, classId);
      return data;
    },
    enabled: !!subjectId && !!classId,
  });

  const createMutation = useMutation({
    mutationFn: async (data) => {
      const response = await createAssignment(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Assignment created!");
      setShowCreate(false);
      setTitle("");
      setDueDate("");
      queryClient.invalidateQueries({ queryKey: ["teacher", "assignments"] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to create assignment"),
  });

  const submissionMutation = useMutation({
    mutationFn: async ({ assignmentId, data }) => {
      const response = await markSubmissions(assignmentId, data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Submissions updated!");
      setSelectedAssignment(null);
      setSubmissions([]);
      queryClient.invalidateQueries({ queryKey: ["teacher", "assignments"] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to update submissions"),
  });

  const handleCreate = () => {
    if (!title || !dueDate) {
      toast.error("Please fill all fields");
      return;
    }
    createMutation.mutate({ subjectId, classId, title, dueDate });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Assignments</h2>
      </div>

      <div className="flex items-end gap-4">
        <div className="flex-1">
          <Select
            label="Subject & Class"
            placeholder="Select subject..."
            options={subjectOptions}
            value={selectedSubject}
            onValueChange={setSelectedSubject}
          />
        </div>
        {selectedSubject && (
          <Button onClick={() => setShowCreate(true)}>
            <Plus className="w-4 h-4" /> Create Assignment
          </Button>
        )}
      </div>

      {/* Assignment List */}
      {!selectedSubject ? (
        <EmptyState title="Select a subject" description="Choose a subject to manage assignments." />
      ) : isLoading ? (
        <LoadingSpinner size="lg" className="mt-16" />
      ) : assignments.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="No assignments"
          description="Create your first assignment above."
        />
      ) : (
        <div className="grid md:grid-cols-2 gap-4">
          {assignments.map((a) => (
            <Card
              key={a.id}
              hover
              onClick={() => setSelectedAssignment(a)}
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium text-text-primary">{a.title}</p>
                  <p className="text-xs text-text-tertiary mt-1">
                    Due: {formatDate(a.dueDate)}
                  </p>
                </div>
                <Badge variant="default">
                  {a.submissions?.length || 0} submissions
                </Badge>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create Modal */}
      <Modal
        open={showCreate}
        onOpenChange={setShowCreate}
        title="Create Assignment"
      >
        <div className="space-y-4">
          <Input
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Assignment title"
          />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Due Date</label>
            <input
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
          <Button onClick={handleCreate} loading={createMutation.isPending} className="w-full">
            Create
          </Button>
        </div>
      </Modal>

      {/* Submission marking modal */}
      <Modal
        open={!!selectedAssignment}
        onOpenChange={(open) => !open && setSelectedAssignment(null)}
        title={`Mark Submissions - ${selectedAssignment?.title || ""}`}
        className="max-w-2xl"
      >
        {selectedAssignment && (
          <div>
            <p className="text-sm text-text-secondary mb-4">
              Update submission status for each student.
            </p>
            {submissions.length > 0 ? (
              <>
                <div className="mb-3">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() =>
                      setSubmissions((prev) =>
                        prev.map((s) => ({ ...s, status: "SUBMITTED" }))
                      )
                    }
                  >
                    Mark all Submitted
                  </Button>
                </div>
                <div className="max-h-80 overflow-y-auto space-y-2">
                  {submissions.map((sub, idx) => (
                    <div
                      key={sub.studentId}
                      className="flex items-center justify-between p-3 bg-bg-primary rounded-xl"
                    >
                      <span className="text-sm text-text-primary">{sub.studentName}</span>
                      <select
                        value={sub.status}
                        onChange={(e) => {
                          const updated = [...submissions];
                          updated[idx] = { ...updated[idx], status: e.target.value };
                          setSubmissions(updated);
                        }}
                        className="bg-bg-secondary border border-border-light rounded-lg px-3 py-1.5 text-xs focus:outline-none"
                      >
                        <option value="SUBMITTED">Submitted</option>
                        <option value="NOT_SUBMITTED">Not Submitted</option>
                        <option value="LATE">Late</option>
                      </select>
                    </div>
                  ))}
                </div>
                <Button
                  className="w-full mt-4"
                  onClick={() =>
                    submissionMutation.mutate({
                      assignmentId: selectedAssignment.id,
                      data: {
                        assignmentId: selectedAssignment.id,
                        submissions: submissions.map((s) => ({
                          studentId: s.studentId,
                          status: s.status,
                        })),
                      },
                    })
                  }
                  loading={submissionMutation.isPending}
                >
                  Save Submissions
                </Button>
              </>
            ) : (
              <p className="text-sm text-text-tertiary text-center py-4">
                No student data available. Submissions will be loaded from the backend.
              </p>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
