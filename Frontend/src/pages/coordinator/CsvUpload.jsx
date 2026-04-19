import { useEffect, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Upload, Download, CheckCircle, AlertCircle } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import {
  uploadStudentsCsv, uploadTeachersCsv,
  downloadStudentTemplate, downloadTeacherTemplate,
} from "../../api/coordinator";
import Card, { CardTitle } from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import FileUpload from "../../components/ui/FileUpload";

export default function CsvUpload() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("CSV Upload"), [setPageTitle]);

  const [studentFile, setStudentFile] = useState(null);
  const [teacherFile, setTeacherFile] = useState(null);
  const [studentResult, setStudentResult] = useState(null);
  const [teacherResult, setTeacherResult] = useState(null);

  const studentMutation = useMutation({
    mutationFn: (file) => uploadStudentsCsv(file),
    onSuccess: (res) => {
      setStudentResult(res.data);
      toast.success("Student CSV uploaded!");
      setStudentFile(null);
    },
    onError: (err) => toast.error(err.response?.data?.message || "Upload failed"),
  });

  const teacherMutation = useMutation({
    mutationFn: (file) => uploadTeachersCsv(file),
    onSuccess: (res) => {
      setTeacherResult(res.data);
      toast.success("Teacher CSV uploaded!");
      setTeacherFile(null);
    },
    onError: (err) => toast.error(err.response?.data?.message || "Upload failed"),
  });

  const handleDownload = async (downloadFn, filename) => {
    try {
      const { data } = await downloadFn();
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error("Failed to download template");
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">CSV Upload</h2>

      <div className="grid md:grid-cols-2 gap-6">
        {/* Student CSV */}
        <Card>
          <CardTitle className="flex items-center gap-2">
            <Upload className="w-4 h-4" /> Student CSV
          </CardTitle>
          <div className="mt-4 space-y-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleDownload(downloadStudentTemplate, "student_template.csv")}
            >
              <Download className="w-4 h-4" /> Download Template
            </Button>
            <FileUpload
              accept={{ "text/csv": [".csv"] }}
              file={studentFile}
              onFileSelect={(f) => setStudentFile(f)}
              onClear={() => setStudentFile(null)}
            />
            {studentFile && (
              <Button
                className="w-full"
                onClick={() => studentMutation.mutate(studentFile)}
                loading={studentMutation.isPending}
              >
                Upload Student CSV
              </Button>
            )}
            {studentResult && (
              <div className="p-3 bg-bg-primary rounded-xl space-y-1">
                <div className="flex items-center gap-2 text-sm">
                  <CheckCircle className="w-4 h-4 text-status-success" />
                  <span className="text-text-primary">
                    {studentResult.successCount || studentResult.created || 0} created
                  </span>
                </div>
                {(studentResult.errorCount || studentResult.errors?.length > 0) && (
                  <div className="flex items-center gap-2 text-sm">
                    <AlertCircle className="w-4 h-4 text-status-error" />
                    <span className="text-text-primary">
                      {studentResult.errorCount || studentResult.errors?.length || 0} errors
                    </span>
                  </div>
                )}
                {studentResult.errors?.length > 0 && (
                  <ul className="mt-2 space-y-1 text-xs text-status-error">
                    {studentResult.errors.slice(0, 5).map((e, i) => (
                      <li key={i}>{typeof e === "string" ? e : JSON.stringify(e)}</li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
        </Card>

        {/* Teacher CSV */}
        <Card>
          <CardTitle className="flex items-center gap-2">
            <Upload className="w-4 h-4" /> Teacher CSV
          </CardTitle>
          <div className="mt-4 space-y-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleDownload(downloadTeacherTemplate, "teacher_template.csv")}
            >
              <Download className="w-4 h-4" /> Download Template
            </Button>
            <FileUpload
              accept={{ "text/csv": [".csv"] }}
              file={teacherFile}
              onFileSelect={(f) => setTeacherFile(f)}
              onClear={() => setTeacherFile(null)}
            />
            {teacherFile && (
              <Button
                className="w-full"
                onClick={() => teacherMutation.mutate(teacherFile)}
                loading={teacherMutation.isPending}
              >
                Upload Teacher CSV
              </Button>
            )}
            {teacherResult && (
              <div className="p-3 bg-bg-primary rounded-xl space-y-1">
                <div className="flex items-center gap-2 text-sm">
                  <CheckCircle className="w-4 h-4 text-status-success" />
                  <span className="text-text-primary">
                    {teacherResult.successCount || teacherResult.created || 0} created
                  </span>
                </div>
                {(teacherResult.errorCount || teacherResult.errors?.length > 0) && (
                  <div className="flex items-center gap-2 text-sm">
                    <AlertCircle className="w-4 h-4 text-status-error" />
                    <span className="text-text-primary">
                      {teacherResult.errorCount || teacherResult.errors?.length || 0} errors
                    </span>
                  </div>
                )}
              </div>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
