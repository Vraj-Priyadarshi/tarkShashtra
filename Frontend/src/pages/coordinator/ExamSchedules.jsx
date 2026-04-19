import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Calendar, Plus } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getExamSchedules, createExamSchedule } from "../../api/coordinator";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Modal from "../../components/ui/Modal";
import EmptyState from "../../components/ui/EmptyState";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import { formatDate } from "../../lib/utils";

export default function ExamSchedules() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Exam Schedules"), [setPageTitle]);
  const queryClient = useQueryClient();

  const [showCreate, setShowCreate] = useState(false);
  const [startDate, setStartDate] = useState("2025-01-01");
  const [endDate, setEndDate] = useState("2025-12-31");
  const [form, setForm] = useState({
    subjectId: "", classId: "", examDate: "", examType: "IA-1",
  });

  const { data: schedules = [], isLoading } = useQuery({
    queryKey: ["coordinator", "exam-schedules", startDate, endDate],
    queryFn: async () => {
      const { data } = await getExamSchedules(startDate, endDate);
      return data;
    },
  });

  const createMutation = useMutation({
    mutationFn: (d) => createExamSchedule(d),
    onSuccess: () => {
      toast.success("Exam scheduled!"); setShowCreate(false);
      setForm({ subjectId: "", classId: "", examDate: "", examType: "IA-1" });
      queryClient.invalidateQueries({ queryKey: ["coordinator", "exam-schedules"] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to create schedule"),
  });

  const handleCreate = (e) => {
    e.preventDefault();
    createMutation.mutate(form);
  };

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Exam Schedules</h2>
        <Button onClick={() => setShowCreate(true)}>
          <Plus className="w-4 h-4" /> Schedule Exam
        </Button>
      </div>

      {/* Date filter */}
      <Card>
        <div className="flex items-end gap-4">
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">From</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">To</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
        </div>
      </Card>

      {schedules.length === 0 ? (
        <EmptyState icon={Calendar} title="No exams scheduled" description="No exam schedules found for the selected period." />
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Date</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Type</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Subject</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Class</th>
                </tr>
              </thead>
              <tbody>
                {schedules.map((s, i) => (
                  <tr key={s.id || i} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 text-text-primary">{formatDate(s.examDate)}</td>
                    <td className="px-4 py-3 text-text-secondary">{s.examType}</td>
                    <td className="px-4 py-3 text-text-primary">{s.subjectName || s.subjectId}</td>
                    <td className="px-4 py-3 text-text-secondary">{s.className || s.classId}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      <Modal open={showCreate} onOpenChange={setShowCreate} title="Schedule Exam">
        <form onSubmit={handleCreate} className="space-y-4">
          <Input label="Subject ID" value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })} required />
          <Input label="Class ID" value={form.classId} onChange={(e) => setForm({ ...form, classId: e.target.value })} required />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Exam Date</label>
            <input
              type="date"
              value={form.examDate}
              onChange={(e) => setForm({ ...form, examDate: e.target.value })}
              required
              className="w-full bg-bg-primary border border-border-light rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-accent-primary/30"
            />
          </div>
          <Input label="Exam Type" value={form.examType} onChange={(e) => setForm({ ...form, examType: e.target.value })} />
          <Button type="submit" loading={createMutation.isPending} className="w-full">Schedule</Button>
        </form>
      </Modal>
    </div>
  );
}
