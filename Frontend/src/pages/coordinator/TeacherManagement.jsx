import { useEffect, useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { UserPlus, Search, ChevronUp, ChevronDown } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getTeachers, addTeacherManual } from "../../api/coordinator";
import Card from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Modal from "../../components/ui/Modal";
import Input from "../../components/ui/Input";
import Badge from "../../components/ui/Badge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

const PAGE_SIZE = 15;

const SORT_OPTIONS = [
  { value: "fullName", label: "Name" },
  { value: "email", label: "Email" },
  { value: "employeeId", label: "Emp ID" },
  { value: "departmentName", label: "Department" },
];

export default function TeacherManagement() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Teacher Management"), [setPageTitle]);
  const queryClient = useQueryClient();

  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [sortCol, setSortCol] = useState("fullName");
  const [sortDir, setSortDir] = useState("asc");
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({
    email: "", password: "", fullName: "", departmentId: "", roles: ["SUBJECT_TEACHER"],
  });

  // Load all teachers for client-side search/sort
  const { data, isLoading } = useQuery({
    queryKey: ["coordinator", "teachers", "all"],
    queryFn: async () => {
      const { data } = await getTeachers(0, 500);
      return data;
    },
    staleTime: 1000 * 60 * 2,
  });

  const addMutation = useMutation({
    mutationFn: (d) => addTeacherManual(d),
    onSuccess: () => {
      toast.success("Teacher added!");
      setShowAdd(false);
      setForm({ email: "", password: "", fullName: "", departmentId: "", roles: ["SUBJECT_TEACHER"] });
      queryClient.invalidateQueries({ queryKey: ["coordinator", "teachers"] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to add teacher"),
  });

  const handleAdd = (e) => {
    e.preventDefault();
    addMutation.mutate(form);
  };

  const allTeachers = data?.content || [];

  const filtered = useMemo(() => {
    const q = search.toLowerCase().trim();
    let result = q
      ? allTeachers.filter(
          (t) =>
            t.fullName?.toLowerCase().includes(q) ||
            t.email?.toLowerCase().includes(q) ||
            t.employeeId?.toLowerCase().includes(q) ||
            t.departmentName?.toLowerCase().includes(q)
        )
      : allTeachers;

    result = [...result].sort((a, b) => {
      let av = a[sortCol] ?? "";
      let bv = b[sortCol] ?? "";
      if (typeof av === "string") av = av.toLowerCase();
      if (typeof bv === "string") bv = bv.toLowerCase();
      if (av < bv) return sortDir === "asc" ? -1 : 1;
      if (av > bv) return sortDir === "asc" ? 1 : -1;
      return 0;
    });

    return result;
  }, [allTeachers, search, sortCol, sortDir]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageTeachers = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Teacher Management</h2>
        <Button onClick={() => setShowAdd(true)}>
          <UserPlus className="w-4 h-4" /> Add Teacher
        </Button>
      </div>

      {/* Search + Sort toolbar */}
      <div className="flex items-center gap-3 flex-wrap">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-tertiary pointer-events-none" />
          <input
            type="text"
            placeholder="Search by name, email, employee ID, dept…"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            className="pl-9 pr-4 py-2.5 text-sm bg-bg-secondary border border-border-light rounded-xl text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 focus:border-accent-primary placeholder:text-text-tertiary w-72"
          />
        </div>
        <div className="flex items-center gap-2 ml-auto">
          <span className="text-sm text-text-tertiary">Sort by:</span>
          <select
            value={sortCol}
            onChange={(e) => { setSortCol(e.target.value); setPage(0); }}
            className="text-sm bg-bg-secondary border border-border-light rounded-xl px-3 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/30 cursor-pointer"
          >
            {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <button
            onClick={() => { setSortDir((d) => d === "asc" ? "desc" : "asc"); setPage(0); }}
            className="flex items-center gap-1.5 px-3 py-2 text-sm bg-bg-secondary border border-border-light rounded-xl text-text-primary hover:border-accent-primary hover:text-accent-primary transition-colors"
          >
            {sortDir === "asc" ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            {sortDir === "asc" ? "A → Z" : "Z → A"}
          </button>
        </div>
      </div>

      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border-light">
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Name</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Email</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Emp ID</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Department</th>
                <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roles</th>
              </tr>
            </thead>
            <tbody>
              {pageTeachers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-12 text-center text-text-tertiary text-sm">
                    {search ? "No teachers match your search." : "No teachers found."}
                  </td>
                </tr>
              ) : pageTeachers.map((t) => (
                <tr key={t.id} className="border-b border-border-light last:border-0 hover:bg-bg-hover/50 transition-colors">
                  <td className="px-4 py-3 font-medium text-text-primary">{t.fullName}</td>
                  <td className="px-4 py-3 text-text-secondary">{t.email}</td>
                  <td className="px-4 py-3 text-text-secondary font-mono text-xs">{t.employeeId || "—"}</td>
                  <td className="px-4 py-3 text-text-secondary">{t.departmentName || "—"}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-1 flex-wrap">
                      {t.roles?.map((r) => (
                        <Badge key={r} variant="default">{r.replace(/_/g, " ")}</Badge>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between px-4 py-3 border-t border-border-light">
          <p className="text-xs text-text-tertiary">
            {filtered.length === 0 ? "0 teachers" : `${page * PAGE_SIZE + 1}–${Math.min((page + 1) * PAGE_SIZE, filtered.length)} of ${filtered.length} teachers`}
            {search && allTeachers.length !== filtered.length && ` (filtered from ${allTeachers.length})`}
          </p>
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
              Previous
            </Button>
            <span className="text-sm text-text-secondary px-2">
              {page + 1} / {totalPages}
            </span>
            <Button variant="ghost" size="sm" onClick={() => setPage((p) => p + 1)} disabled={page + 1 >= totalPages}>
              Next
            </Button>
          </div>
        </div>
      </Card>

      <Modal open={showAdd} onOpenChange={setShowAdd} title="Add Teacher">
        <form onSubmit={handleAdd} className="space-y-4">
          <Input label="Full Name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
          <Input label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <Input label="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          <Input label="Department ID" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })} />
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-text-primary">Roles</label>
            <div className="flex gap-4">
              {["SUBJECT_TEACHER", "FACULTY_MENTOR"].map((role) => (
                <label key={role} className="flex items-center gap-2 text-sm cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.roles.includes(role)}
                    onChange={(e) => {
                      setForm((prev) => ({
                        ...prev,
                        roles: e.target.checked
                          ? [...prev.roles, role]
                          : prev.roles.filter((r) => r !== role),
                      }));
                    }}
                    className="rounded"
                  />
                  {role.replace(/_/g, " ")}
                </label>
              ))}
            </div>
          </div>
          <Button type="submit" loading={addMutation.isPending} className="w-full">Add Teacher</Button>
        </form>
      </Modal>
    </div>
  );
}
