'use client'

import { useState } from 'react'
import Link from 'next/link'
import { format } from 'date-fns'
import { ArrowLeft, ChevronDown, ChevronUp, Download, Eye, FileText, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { ContractStatusBadge } from '@/components/shared/StatusBadge'
import { EmptyState } from '@/components/shared/EmptyState'
import { NotificationHistoryTable } from './NotificationHistoryTable'
import { useContract, useContractDocuments } from '@/lib/hooks/useContracts'
import { apiClient } from '@/lib/api/client'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import type { ContractDocumentResponse } from '@/lib/types'

function DetailRow({ label, value }: { label: string; value?: React.ReactNode }) {
  return (
    <div className="flex justify-between py-2 text-sm border-b last:border-0">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium text-right">{value ?? <span className="text-muted-foreground italic">—</span>}</span>
    </div>
  )
}

function ExpiryIndicator({ days, status }: { days: number; status: string }) {
  if (status !== 'ACTIVE') return null
  if (days <= 0) return <Badge className="border-0 bg-red-100 text-red-800">Expired</Badge>
  if (days <= 7) return <Badge className="border-0 bg-red-100 text-red-800">{days} days left</Badge>
  if (days <= 30) return <Badge className="border-0 bg-amber-100 text-amber-800">{days} days left</Badge>
  return <span className="text-sm font-medium">{days} days left</span>
}

interface ContractDetailViewProps {
  id: string
}

export function ContractDetailView({ id }: ContractDetailViewProps) {
  const { data: contract, isLoading, isError } = useContract(id)
  const { data: documents = [] } = useContractDocuments(id)
  const [termsExpanded, setTermsExpanded] = useState(false)
  const [previewDoc, setPreviewDoc] = useState<{ url: string; name: string; type: string } | null>(null)

  async function fetchDocBlob(doc: ContractDocumentResponse): Promise<string> {
    const res = await apiClient.get(
      `/api/v1/contracts/${doc.contractId}/documents/${doc.id}/download`,
      { responseType: 'blob' }
    )
    return URL.createObjectURL(res.data as Blob)
  }

  async function handleDownload(doc: ContractDocumentResponse) {
    const url = await fetchDocBlob(doc)
    const a = document.createElement('a')
    a.href = url
    a.download = doc.fileName
    a.click()
    URL.revokeObjectURL(url)
  }

  async function handlePreview(doc: ContractDocumentResponse) {
    const url = await fetchDocBlob(doc)
    setPreviewDoc({ url, name: doc.fileName, type: doc.fileType })
  }

  function closePreview() {
    if (previewDoc) URL.revokeObjectURL(previewDoc.url)
    setPreviewDoc(null)
  }

  if (isLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-8 w-48" />
        <Card><CardContent className="pt-6 space-y-3">
          {[...Array(8)].map((_, i) => <Skeleton key={i} className="h-6 w-full" />)}
        </CardContent></Card>
      </div>
    )
  }

  if (isError || !contract) {
    return (
      <div className="p-6">
        <EmptyState
          title="Contract not found"
          description="This contract doesn't exist or could not be loaded."
          action={{ label: 'Back to contracts', href: '/contracts' }}
        />
      </div>
    )
  }

  const canRenew = contract.status === 'ACTIVE'
  const canTerminate = contract.status === 'ACTIVE' || contract.status === 'PENDING'

  return (
    <div className="space-y-6 p-6">
      {/* Page header */}
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" asChild>
            <Link href="/contracts">
              <ArrowLeft className="h-4 w-4" />
              Back
            </Link>
          </Button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold tracking-tight font-mono">{contract.contractNumber}</h1>
              <ContractStatusBadge status={contract.status} />
            </div>
            <p className="text-sm text-muted-foreground">
              <Link
                href={`/collaborators/${contract.collaboratorId}`}
                className="text-indigo-600 hover:underline"
              >
                View Collaborator
              </Link>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {canRenew && (
            <Button variant="outline" asChild>
              <Link href={`/contracts/${id}/renew`}>Renew</Link>
            </Button>
          )}
          {canTerminate && (
            <Button variant="destructive" asChild>
              <Link href={`/contracts/${id}/terminate`}>Terminate</Link>
            </Button>
          )}
        </div>
      </div>

      {/* Main detail card */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Contract Information</CardTitle>
        </CardHeader>
        <CardContent>
          <DetailRow
            label="Type"
            value={<Badge variant="outline">{CONTRACT_TYPE_LABELS[contract.contractType]}</Badge>}
          />
          <DetailRow label="Collaborator ID" value={
            <Link href={`/collaborators/${contract.collaboratorId}`} className="text-indigo-600 hover:underline font-mono text-xs">
              {contract.collaboratorId}
            </Link>
          } />
          <DetailRow label="Start Date" value={format(new Date(contract.startDate), 'MMM d, yyyy')} />
          <DetailRow label="End Date" value={format(new Date(contract.endDate), 'MMM d, yyyy')} />
          <DetailRow
            label="Days Until Expiry"
            value={<ExpiryIndicator days={contract.daysUntilExpiry} status={contract.status} />}
          />
          <DetailRow
            label="Salary"
            value={
              contract.salary
                ? `${contract.salary.toLocaleString()} ${contract.currency}`
                : undefined
            }
          />
          <DetailRow label="Notice Period" value={`${contract.noticePeriodDays} days`} />
          <DetailRow label="Auto Renewal" value={contract.autoRenewal ? 'Yes' : 'No'} />
          <DetailRow label="Renewal Count" value={String(contract.renewalCount)} />
          {contract.previousContractId && (
            <DetailRow
              label="Previous Contract"
              value={
                <Link
                  href={`/contracts/${contract.previousContractId}`}
                  className="text-indigo-600 hover:underline font-mono text-xs"
                >
                  View previous →
                </Link>
              }
            />
          )}
          <DetailRow label="Created" value={format(new Date(contract.createdAt), 'MMM d, yyyy')} />
        </CardContent>
      </Card>

      {/* Terms & Conditions */}
      {contract.termsAndConditions && (
        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between">
              <CardTitle className="text-base">Terms & Conditions</CardTitle>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setTermsExpanded((v) => !v)}
              >
                {termsExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                {termsExpanded ? 'Collapse' : 'Expand'}
              </Button>
            </div>
          </CardHeader>
          {termsExpanded && (
            <CardContent>
              <p className="text-sm whitespace-pre-wrap text-muted-foreground leading-relaxed">
                {contract.termsAndConditions}
              </p>
            </CardContent>
          )}
        </Card>
      )}

      {/* Documents */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Documents</CardTitle>
        </CardHeader>
        <CardContent>
          {documents.length === 0 ? (
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <FileText className="h-5 w-5" />
              <span>No documents attached. Upload one from the Renew Contract page.</span>
            </div>
          ) : (
            <ul className="divide-y">
              {documents.map((doc) => (
                <li key={doc.id} className="flex items-center gap-3 py-2.5 text-sm">
                  <FileText className="h-4 w-4 shrink-0 text-muted-foreground" />
                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">{doc.fileName}</p>
                    <p className="text-xs text-muted-foreground">
                      {(doc.fileSize / 1024).toFixed(1)} KB · {format(new Date(doc.uploadedAt), 'MMM d, yyyy HH:mm')}
                    </p>
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    <Button variant="ghost" size="sm" onClick={() => handlePreview(doc)} title="Preview">
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleDownload(doc)} title="Download">
                      <Download className="h-4 w-4" />
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      {/* Preview modal */}
      <Dialog open={!!previewDoc} onOpenChange={(open) => { if (!open) closePreview() }}>
        <DialogContent className="max-w-4xl w-full h-[80vh] flex flex-col p-0">
          <DialogHeader className="px-4 pt-4 pb-2 flex-row items-center justify-between border-b">
            <DialogTitle className="text-sm font-medium truncate max-w-[80%]">
              {previewDoc?.name}
            </DialogTitle>
            <Button variant="ghost" size="sm" onClick={closePreview}>
              <X className="h-4 w-4" />
            </Button>
          </DialogHeader>
          <div className="flex-1 overflow-hidden">
            {previewDoc?.type.startsWith('image/') ? (
              <div className="h-full flex items-center justify-center p-4 bg-muted/30">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={previewDoc.url} alt={previewDoc.name} className="max-h-full max-w-full object-contain rounded" />
              </div>
            ) : previewDoc?.type === 'application/pdf' ? (
              <iframe src={previewDoc.url} className="w-full h-full border-0" title={previewDoc.name} />
            ) : (
              <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
                Preview not available for this file type.
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <Separator />

      {/* Notification history */}
      <div className="space-y-3">
        <h2 className="text-base font-semibold">Notification History</h2>
        <NotificationHistoryTable contractId={id} />
      </div>
    </div>
  )
}
