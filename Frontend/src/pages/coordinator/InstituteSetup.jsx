import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import {
  getDepartments, createDepartment,
  getClasses, createClass,
  getSubjects, createSubject,
  mapSubjectToClass, mapTeacherToSubject,
} from "../../api/coordinator";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Input from "../../components/ui/Input";
import Select from "../../components/ui/Select";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "../../components/ui/Tabs";
import Badge from "../../components/ui/Badge";
import Modal from "../../components/ui/Modal";
import EmptyState from "../../components/ui/EmptyState";
import LoadingSpinner from "../../components/ui/LoadingSpinner";

export default function InstituteSetup() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Institute Setup"), [setPageTitle]);
  const queryClient = useQueryClient();

  // Department state
  const [showDept, setShowDept] = useState(false);
  const [deptName, setDeptName] = useState("");
  const [deptCode, setDeptCode] = useState("");

  // Class state
  const [showClass, setShowClass] = useState(false);
  const [classDeptId, setClassDeptId] = useState("");
  const [className, setClassName] = useState("");
  const [classSemester, setClassSemester] = useState(1);
  const [classYear, setClassYear] = useState("2025-26");

  // Subject state
  const [showSubject, setShowSubject] = useState(false);
  const [subjDeptId, setSubjDeptId] = useState("");
  const [subjName, setSubjName] = useState("");
  const [subjCode, setSubjCode] = useState("");

  // Mapping state
  const [showMapSC, setShowMapSC] = useState(false);
  const [mapSubjectId, setMapSubjectId] = useState("");
  const [mapClassId, setMapClassId] = useState("");
  const [mapSemester, setMapSemester] = useState(1);
  const [mapYear, setMapYear] = useState("2025-26");

  const [showMapTS, setShowMapTS] = useState(false);
  const [mapTeacherId, setMapTeacherId] = useState("");
  const [mapTSubjectId, setMapTSubjectId] = useState("");
  const [mapTClassId, setMapTClassId] = useState("");
  const [mapTYear, setMapTYear] = useState("2025-26");

  // Data queries
  const { data: departments = [], isLoading: deptLoading } = useQuery({
    queryKey: ["coordinator", "departments"],
    queryFn: async () => { const { data } = await getDepartments(); return Array.isArray(data) ? data : []; },
  });

  const { data: classes = [] } = useQuery({
    queryKey: ["coordinator", "classes"],
    queryFn: async () => { const { data } = await getClasses(); return Array.isArray(data) ? data : []; },
  });

  const { data: subjects = [] } = useQuery({
    queryKey: ["coordinator", "subjects"],
    queryFn: async () => { const { data } = await getSubjects(); return Array.isArray(data) ? data : []; },
  });

  const deptOptions = departments.map((d) => ({ value: d.id, label: `${d.name} (${d.code})` }));
  const classOptions = classes.map((c) => ({ value: c.id, label: c.name }));
  const subjOptions = subjects.map((s) => ({ value: s.id, label: `${s.name} (${s.code})` }));

  // Mutations
  const deptMutation = useMutation({
    mutationFn: () => createDepartment(deptName, deptCode),
    onSuccess: () => {
      toast.success("Department created!"); setShowDept(false); setDeptName(""); setDeptCode("");
      queryClient.invalidateQueries({ queryKey: ["coordinator", "departments"] });
    },
    onError: (e) => toast.error(e.response?.data?.message || "Failed"),
  });

  const classMutation = useMutation({
    mutationFn: () => createClass(classDeptId, className, classSemester, classYear),
    onSuccess: () => {
      toast.success("Class created!"); setShowClass(false); setClassName("");
      queryClient.invalidateQueries({ queryKey: ["coordinator", "classes"] });
    },
    onError: (e) => toast.error(e.response?.data?.message || "Failed"),
  });

  const subjMutation = useMutation({
    mutationFn: () => createSubject(subjDeptId, subjName, subjCode),
    onSuccess: () => {
      toast.success("Subject created!"); setShowSubject(false); setSubjName(""); setSubjCode("");
      queryClient.invalidateQueries({ queryKey: ["coordinator", "subjects"] });
    },
    onError: (e) => toast.error(e.response?.data?.message || "Failed"),
  });

  const mapSCMutation = useMutation({
    mutationFn: () => mapSubjectToClass(mapSubjectId, mapClassId, mapSemester, mapYear),
    onSuccess: () => { toast.success("Subject mapped to class!"); setShowMapSC(false); },
    onError: (e) => toast.error(e.response?.data?.message || "Failed"),
  });

  const mapTSMutation = useMutation({
    mutationFn: () => mapTeacherToSubject(mapTeacherId, mapTSubjectId, mapTClassId, mapTYear),
    onSuccess: () => { toast.success("Teacher mapped to subject!"); setShowMapTS(false); },
    onError: (e) => toast.error(e.response?.data?.message || "Failed"),
  });

  if (deptLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Institute Setup</h2>

      <Tabs defaultValue="departments">
        <TabsList>
          <TabsTrigger value="departments">Departments</TabsTrigger>
          <TabsTrigger value="classes">Classes</TabsTrigger>
          <TabsTrigger value="subjects">Subjects</TabsTrigger>
          <TabsTrigger value="mappings">Mappings</TabsTrigger>
        </TabsList>

        {/* Departments */}
        <TabsContent value="departments">
          <div className="flex justify-end mb-4">
            <Button size="sm" onClick={() => setShowDept(true)}><Plus className="w-4 h-4" /> Add Department</Button>
          </div>
          {departments.length === 0 ? (
            <EmptyState title="No departments" description="Create your first department." />
          ) : (
            <div className="grid md:grid-cols-3 gap-4">
              {departments.map((d) => (
                <Card key={d.id}>
                  <p className="text-sm font-medium text-text-primary">{d.name}</p>
                  <Badge variant="default" className="mt-1">{d.code}</Badge>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Classes */}
        <TabsContent value="classes">
          <div className="flex justify-end mb-4">
            <Button size="sm" onClick={() => setShowClass(true)}><Plus className="w-4 h-4" /> Add Class</Button>
          </div>
          {classes.length === 0 ? (
            <EmptyState title="No classes" description="Create your first class." />
          ) : (
            <div className="grid md:grid-cols-3 gap-4">
              {classes.map((c) => (
                <Card key={c.id}>
                  <p className="text-sm font-medium text-text-primary">{c.name}</p>
                  <p className="text-xs text-text-tertiary mt-1">
                    Sem {c.semester} · {c.academicYear}
                  </p>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Subjects */}
        <TabsContent value="subjects">
          <div className="flex justify-end mb-4">
            <Button size="sm" onClick={() => setShowSubject(true)}><Plus className="w-4 h-4" /> Add Subject</Button>
          </div>
          {subjects.length === 0 ? (
            <EmptyState title="No subjects" description="Create your first subject." />
          ) : (
            <div className="grid md:grid-cols-3 gap-4">
              {subjects.map((s) => (
                <Card key={s.id}>
                  <p className="text-sm font-medium text-text-primary">{s.name}</p>
                  <Badge variant="default" className="mt-1">{s.code}</Badge>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Mappings */}
        <TabsContent value="mappings">
          <div className="flex gap-3 mb-4">
            <Button size="sm" onClick={() => setShowMapSC(true)}>Map Subject → Class</Button>
            <Button size="sm" variant="secondary" onClick={() => setShowMapTS(true)}>Map Teacher → Subject</Button>
          </div>
          <Card>
            <p className="text-sm text-text-secondary text-center py-8">
              Use the buttons above to map subjects to classes and teachers to subjects.
            </p>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Department Modal */}
      <Modal open={showDept} onOpenChange={setShowDept} title="Add Department">
        <div className="space-y-4">
          <Input label="Name" value={deptName} onChange={(e) => setDeptName(e.target.value)} required />
          <Input label="Code" value={deptCode} onChange={(e) => setDeptCode(e.target.value)} required />
          <Button onClick={() => deptMutation.mutate()} loading={deptMutation.isPending} className="w-full">Create</Button>
        </div>
      </Modal>

      {/* Class Modal */}
      <Modal open={showClass} onOpenChange={setShowClass} title="Add Class">
        <div className="space-y-4">
          <Select label="Department" options={deptOptions} value={classDeptId} onValueChange={setClassDeptId} />
          <Input label="Name" value={className} onChange={(e) => setClassName(e.target.value)} required />
          <Input label="Semester" type="number" min="1" max="8" value={classSemester} onChange={(e) => setClassSemester(parseInt(e.target.value))} />
          <Input label="Academic Year" value={classYear} onChange={(e) => setClassYear(e.target.value)} />
          <Button onClick={() => classMutation.mutate()} loading={classMutation.isPending} className="w-full">Create</Button>
        </div>
      </Modal>

      {/* Subject Modal */}
      <Modal open={showSubject} onOpenChange={setShowSubject} title="Add Subject">
        <div className="space-y-4">
          <Select label="Department" options={deptOptions} value={subjDeptId} onValueChange={setSubjDeptId} />
          <Input label="Name" value={subjName} onChange={(e) => setSubjName(e.target.value)} required />
          <Input label="Code" value={subjCode} onChange={(e) => setSubjCode(e.target.value)} required />
          <Button onClick={() => subjMutation.mutate()} loading={subjMutation.isPending} className="w-full">Create</Button>
        </div>
      </Modal>

      {/* Map Subject→Class Modal */}
      <Modal open={showMapSC} onOpenChange={setShowMapSC} title="Map Subject to Class">
        <div className="space-y-4">
          <Select label="Subject" options={subjOptions} value={mapSubjectId} onValueChange={setMapSubjectId} />
          <Select label="Class" options={classOptions} value={mapClassId} onValueChange={setMapClassId} />
          <Input label="Semester" type="number" min="1" max="8" value={mapSemester} onChange={(e) => setMapSemester(parseInt(e.target.value))} />
          <Input label="Academic Year" value={mapYear} onChange={(e) => setMapYear(e.target.value)} />
          <Button onClick={() => mapSCMutation.mutate()} loading={mapSCMutation.isPending} className="w-full">Map</Button>
        </div>
      </Modal>

      {/* Map Teacher→Subject Modal */}
      <Modal open={showMapTS} onOpenChange={setShowMapTS} title="Map Teacher to Subject">
        <div className="space-y-4">
          <Input label="Teacher ID" value={mapTeacherId} onChange={(e) => setMapTeacherId(e.target.value)} />
          <Select label="Subject" options={subjOptions} value={mapTSubjectId} onValueChange={setMapTSubjectId} />
          <Select label="Class" options={classOptions} value={mapTClassId} onValueChange={setMapTClassId} />
          <Input label="Academic Year" value={mapTYear} onChange={(e) => setMapTYear(e.target.value)} />
          <Button onClick={() => mapTSMutation.mutate()} loading={mapTSMutation.isPending} className="w-full">Map</Button>
        </div>
      </Modal>
    </div>
  );
}
