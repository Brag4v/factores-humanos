'use client'

import { useCallback } from 'react'
import { useDropzone } from 'react-dropzone'
import { Upload, X, FileText } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

const MAX_SIZE = 10 * 1024 * 1024 // 10 MB

interface FileDropzoneProps {
  file: File | null
  onFileChange: (file: File | null) => void
  error?: string
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function FileDropzone({ file, onFileChange, error }: FileDropzoneProps) {
  const onDrop = useCallback(
    (accepted: File[]) => {
      if (accepted.length) onFileChange(accepted[0])
    },
    [onFileChange]
  )

  const { getRootProps, getInputProps, isDragActive, fileRejections } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'image/png': ['.png'],
    },
    maxSize: MAX_SIZE,
    multiple: false,
  })

  const rejectionMessage = fileRejections[0]?.errors[0]?.message

  if (file) {
    return (
      <div className="flex items-center justify-between rounded-lg border bg-muted/30 px-4 py-3">
        <div className="flex items-center gap-3">
          <FileText className="h-5 w-5 text-indigo-600 shrink-0" />
          <div>
            <p className="text-sm font-medium">{file.name}</p>
            <p className="text-xs text-muted-foreground">{formatBytes(file.size)}</p>
          </div>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-muted-foreground hover:text-destructive"
          onClick={() => onFileChange(null)}
          aria-label="Remove file"
        >
          <X className="h-4 w-4" />
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-1.5">
      <div
        {...getRootProps()}
        className={cn(
          'flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed px-6 py-10 text-center transition-colors',
          isDragActive
            ? 'border-indigo-500 bg-indigo-50'
            : 'border-muted-foreground/25 hover:border-muted-foreground/50 hover:bg-muted/30'
        )}
      >
        <input {...getInputProps()} />
        <Upload className="h-8 w-8 text-muted-foreground" />
        <div>
          <p className="text-sm font-medium">
            {isDragActive ? 'Drop the file here' : 'Drop PDF, DOCX or PNG here'}
          </p>
          <p className="text-xs text-muted-foreground mt-1">or click to browse · max 10 MB</p>
        </div>
      </div>
      {(error || rejectionMessage) && (
        <p className="text-xs text-destructive">{error ?? rejectionMessage}</p>
      )}
    </div>
  )
}
