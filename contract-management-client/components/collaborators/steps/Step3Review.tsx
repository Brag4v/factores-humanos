import { format } from 'date-fns'
import { ArrowLeft, CheckCircle2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { CollaboratorStatusBadge } from '@/components/shared/StatusBadge'
import { COLLABORATOR_STATUS_LABELS, CONTRACT_TYPE_LABELS } from '@/constants'
import type { CollaboratorFormValues } from '@/lib/schemas/collaborator.schema'
import type { ContractFormValues } from '@/lib/schemas/contract.schema'

interface ReviewRowProps {
  label: string
  value?: string | null
}

function ReviewRow({ label, value }: ReviewRowProps) {
  return (
    <div className="flex justify-between py-1.5 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium text-right">{value ?? <span className="text-muted-foreground italic">—</span>}</span>
    </div>
  )
}

interface Step3Props {
  collaboratorData: CollaboratorFormValues
  contractData: ContractFormValues | null
  onBack: () => void
  onConfirm: () => void
  isSubmitting: boolean
}

export function Step3Review({ collaboratorData, contractData, onBack, onConfirm, isSubmitting }: Step3Props) {
  const formatDate = (d?: string) => (d ? format(new Date(d), 'MMM d, yyyy') : undefined)

  return (
    <div className="space-y-4">
      {/* Collaborator summary */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Collaborator</CardTitle>
          <CardDescription>Review the information before creating.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-0.5 divide-y">
          <ReviewRow label="National ID" value={collaboratorData.nationalId} />
          <ReviewRow label="Full Name" value={`${collaboratorData.firstName} ${collaboratorData.lastName}`} />
          <ReviewRow label="Email" value={collaboratorData.email} />
          <ReviewRow label="Phone" value={collaboratorData.phone || undefined} />
          <ReviewRow label="Department" value={collaboratorData.department || undefined} />
          <ReviewRow label="Position" value={collaboratorData.position} />
          <ReviewRow label="Hire Date" value={formatDate(collaboratorData.hireDate)} />
          <div className="flex justify-between py-1.5 text-sm">
            <span className="text-muted-foreground">Status</span>
            <CollaboratorStatusBadge status={collaboratorData.status} />
          </div>
        </CardContent>
      </Card>

      {/* Contract summary */}
      {contractData ? (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Contract</CardTitle>
          </CardHeader>
          <CardContent className="space-y-0.5 divide-y">
            <div className="flex justify-between py-1.5 text-sm">
              <span className="text-muted-foreground">Type</span>
              <Badge variant="outline">{CONTRACT_TYPE_LABELS[contractData.contractType]}</Badge>
            </div>
            <ReviewRow label="Start Date" value={formatDate(contractData.startDate)} />
            <ReviewRow label="End Date" value={formatDate(contractData.endDate)} />
            <ReviewRow
              label="Salary"
              value={
                contractData.salary
                  ? `${contractData.salary.toLocaleString()} ${contractData.currency}`
                  : undefined
              }
            />
            <ReviewRow label="Notice Period" value={`${contractData.noticePeriodDays} days`} />
            <ReviewRow label="Auto Renewal" value={contractData.autoRenewal ? 'Yes' : 'No'} />
          </CardContent>
        </Card>
      ) : (
        <Card className="border-dashed">
          <CardContent className="py-6 text-center text-sm text-muted-foreground">
            No contract — you can add one later from the collaborator detail page.
          </CardContent>
        </Card>
      )}

      <Separator />

      {/* Actions */}
      <div className="flex items-center justify-between">
        <Button type="button" variant="outline" onClick={onBack} disabled={isSubmitting}>
          <ArrowLeft className="h-4 w-4" />
          Back
        </Button>
        <Button onClick={onConfirm} disabled={isSubmitting} className="min-w-[160px]">
          <CheckCircle2 className="h-4 w-4" />
          {isSubmitting ? 'Creating…' : 'Confirm & Create'}
        </Button>
      </div>
    </div>
  )
}
