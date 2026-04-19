import { useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { Upload, X, FileText } from "lucide-react";
import { cn } from "../../lib/utils";

export default function FileUpload({
  onFileSelect,
  accept = { "text/csv": [".csv"] },
  maxSize = 10 * 1024 * 1024, // 10MB
  file,
  onClear,
  label,
  className,
}) {
  const onDrop = useCallback(
    (acceptedFiles) => {
      if (acceptedFiles.length > 0) {
        onFileSelect(acceptedFiles[0]);
      }
    },
    [onFileSelect]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept,
    maxSize,
    multiple: false,
  });

  if (file) {
    return (
      <div className={cn("border border-border-light rounded-xl p-4", className)}>
        {label && <p className="text-sm font-medium text-text-primary mb-2">{label}</p>}
        <div className="flex items-center gap-3 bg-bg-primary rounded-lg p-3">
          <FileText className="w-5 h-5 text-accent-primary flex-shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-text-primary truncate">{file.name}</p>
            <p className="text-xs text-text-tertiary">
              {(file.size / 1024).toFixed(1)} KB
            </p>
          </div>
          <button
            onClick={onClear}
            className="p-1 hover:bg-bg-hover rounded-lg transition-colors"
          >
            <X className="w-4 h-4 text-text-tertiary" />
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={className}>
      {label && <p className="text-sm font-medium text-text-primary mb-2">{label}</p>}
      <div
        {...getRootProps()}
        className={cn(
          "border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-colors duration-200",
          isDragActive
            ? "border-accent-primary bg-accent-primary/5"
            : "border-border-medium hover:border-accent-primary/50 hover:bg-bg-hover"
        )}
      >
        <input {...getInputProps()} />
        <Upload className="w-8 h-8 text-text-tertiary mx-auto mb-3" />
        <p className="text-sm text-text-primary font-medium">
          {isDragActive ? "Drop the file here" : "Drag & drop a file here"}
        </p>
        <p className="text-xs text-text-tertiary mt-1">or click to browse</p>
        <p className="text-xs text-text-tertiary mt-2">
          Max file size: {maxSize / (1024 * 1024)}MB
        </p>
      </div>
    </div>
  );
}
