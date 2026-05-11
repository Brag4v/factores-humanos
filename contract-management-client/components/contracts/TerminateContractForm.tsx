'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { format } from 'date-fns'
import { toast } from 'sonner'
import { AlertTriangle, ArrowLeft } from 'lucide-react'
import { terminationSchema, type TerminationFormValues } from '@/lib/schemas/contract.schema'
import { useContract, useTerminateContract } from '@/lib/hooks/useContracts'
import { useCollaborator } from '@/lib/hooks/useCollaborators'
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { ContractStatusBadge } from '@/components/shared/StatusBadge'
import { CONTRACT_TYPE_LABELS } from '@/constants'

interface TerminateContractFormProps {
  contractId: string
}

export function TerminateContractForm({ contractId }: TerminateContractFormProps) {
  const router = useRouter()
  const { data: contract, isLoading: loadingContract } = useContract(contractId)
  const { data: collaborator } = useCollaborator(contract?.collaboratorId ?? '')
  const terminateContract = useTerminateContract(contractId)

  const today = new Date().toISOString().split('T')[0]
  const alreadyClosed =
    contract?.status === 'TERMINATED' || contract?.status === 'EXPIRED' || contract?.status === 'RENEWED'

  const form = useForm<TerminationFormValues>({
    resolver: zodResolver(terminationSchema),
    defaultValues: {
      reason: '',
      effectiveDate: today,
      additionalNotes: '',
    },
  })

  async function onSubmit(values: TerminationFormValues) {
    await terminateContract.mutateAsync(
      {
        reason: values.reason,
        effectiveDate: values.effectiveDate,
        additionalNotes: values.additionalNotes || undefined,
      },
      {
        onSuccess: () => {
          toast.success('Contract terminated successfully.')
          router.push('/contracts')
        },
        onError: (err: unknown) => {
          toast.error((err as Error)?.message ?? 'Failed to terminate contract. Please try again.')
        },
      }
    )
  }

  if (loadingContract) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  if (!contract) {
    return (
      <EmptyState
        title="Contract not found"
        description="This contract doesn't exist or could not be loaded."
        action={{ label: 'Back to contracts', href: '/contracts' }}
      />
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" asChild>
            <Link href={`/contracts/${contractId}`}>
              <ArrowLeft className="h-4 w-4" />
              Cancel
            </Link>
          </Button>
          <h1 className="text-2xl font-bold tracking-tight">Terminate Contract</h1>
        </div>
        <ContractStatusBadge status={contract.status} />
      </div>

      {/* Already-closed guard */}
      {alreadyClosed && (
        <Card className="border-amber-200 bg-amber-50">
          <CardContent className="pt-4 pb-4">
            <div className="flex items-center gap-3 text-amber-800">
              <AlertTriangle className="h-5 w-5 shrink-0" />
              <p className="text-sm font-medium">
                This contract is already <strong>{contract.status.toLowerCase()}</strong> and cannot be terminated again.
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Contract summary */}
      <Card className={alreadyClosed ? 'opacity-60' : 'border-red-200'}>
        <CardContent className="pt-5 pb-5">
          <div className="flex items-start gap-3">
            {!alreadyClosed && (
              <AlertTriangle className="h-5 w-5 text-red-600 shrink-0 mt-0.5" />
            )}
            <div className="space-y-3 flex-1">
              {!alreadyClosed && (
                <p className="text-sm font-semibold text-red-800">
                  You are about to permanently terminate this contract. This action cannot be undone.
                </p>
              )}
              <div className="grid gap-2 rounded-md border bg-muted/30 p-4 text-sm sm:grid-cols-2">
                <div className="space-y-1.5">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Contract</span>
                    <span className="font-mono font-medium">{contract.contractNumber}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Collaborator</span>
                    <Link
                      href={`/collaborators/${contract.collaboratorId}`}
                      className="text-indigo-600 hover:underline font-medium"
                    >
                      {collaborator?.fullName ?? contract.collaboratorId}
                    </Link>
                  </div>
                </div>
                <div className="space-y-1.5">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Type</span>
                    <Badge variant="outline">{CONTRACT_TYPE_LABELS[contract.contractType]}</Badge>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">End Date</span>
                    <span className="font-medium">
                      {format(new Date(contract.endDate), 'MMM d, yyyy')}
                      {contract.status === 'ACTIVE' && contract.daysUntilExpiry > 0 && (
                        <span className="text-muted-foreground font-normal ml-1.5">
                          ({contract.daysUntilExpiry}d remaining)
                        </span>
                      )}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Termination form */}
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
          <FormField
            control={form.control}
            name="reason"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Reason for Termination *</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Describe the reason for terminating this contract…"
                    className="min-h-[100px]"
                    disabled={alreadyClosed}
                    {...field}
                  />
                </FormControl>
                <FormDescription>Required. Max 1,000 characters.</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="effectiveDate"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Effective Date *</FormLabel>
                <FormControl>
                  <Input type="date" disabled={alreadyClosed} {...field} />
                </FormControl>
                <FormDescription>The date on which the termination takes effect.</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="additionalNotes"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Additional Notes</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Any additional context or notes…"
                    className="min-h-[80px]"
                    disabled={alreadyClosed}
                    {...field}
                  />
                </FormControl>
                <FormDescription>Optional. Max 2,000 characters.</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <div className="flex items-center justify-between pt-2">
            <Button type="button" variant="outline" asChild>
              <Link href={`/contracts/${contractId}`}>
                <ArrowLeft className="h-4 w-4" />
                Cancel
              </Link>
            </Button>
            <Button
              type="submit"
              variant="destructive"
              disabled={alreadyClosed || terminateContract.isPending}
              className="min-w-[180px]"
            >
              {terminateContract.isPending ? 'Terminating…' : 'Terminate Contract'}
            </Button>
          </div>
        </form>
      </Form>
    </div>
  )
}
