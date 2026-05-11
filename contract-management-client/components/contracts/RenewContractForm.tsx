'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { format } from 'date-fns'
import { toast } from 'sonner'
import { ArrowLeft, Loader2 } from 'lucide-react'
import Link from 'next/link'
import { renewalSchema, type RenewalFormValues } from '@/lib/schemas/contract.schema'
import { useContract, useRenewContract } from '@/lib/hooks/useContracts'
import { useRenewalEligibility, useAverageRating } from '@/lib/hooks/useCollaborators'
import { apiClient } from '@/lib/api/client'
import { EligibilityGate } from './EligibilityGate'
import { FileDropzone } from '@/components/shared/FileDropzone'
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Separator } from '@/components/ui/separator'
import { EmptyState } from '@/components/shared/EmptyState'

interface RenewContractFormProps {
  contractId: string
}

export function RenewContractForm({ contractId }: RenewContractFormProps) {
  const router = useRouter()
  const [file, setFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)

  const { data: contract, isLoading: loadingContract } = useContract(contractId)
  const { data: isEligible, isLoading: loadingEligibility } = useRenewalEligibility(
    contract?.collaboratorId ?? ''
  )
  const { data: avgRating } = useAverageRating(contract?.collaboratorId ?? '')

  const renewContract = useRenewContract(contractId)

  const form = useForm<RenewalFormValues>({
    resolver: zodResolver(renewalSchema),
    defaultValues: {
      newEndDate: '',
      newSalary: undefined,
      newTermsAndConditions: '',
      autoRenewal: false,
      noticePeriodDays: 30,
      renewalNotes: '',
    },
  })

  const isSubmitting = renewContract.isPending || isUploading

  async function onSubmit(values: RenewalFormValues) {
    let result
    try {
      result = await renewContract.mutateAsync({
        newEndDate: values.newEndDate,
        newSalary: values.newSalary || undefined,
        newTermsAndConditions: values.newTermsAndConditions || undefined,
        autoRenewal: values.autoRenewal,
        noticePeriodDays: values.noticePeriodDays,
        renewalNotes: values.renewalNotes || undefined,
      })
    } catch (err) {
      toast.error((err as Error)?.message ?? 'Failed to renew contract. Please try again.')
      return
    }

    if (!result) return

    if (file) {
      setIsUploading(true)
      try {
        const formData = new FormData()
        formData.append('file', file)
        await apiClient.post(`/api/v1/contracts/${result.id}/documents`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        toast.success('Contract renewed and document uploaded successfully.')
      } catch {
        toast.warning(
          'Contract renewed. Document upload failed — you can upload it from the contract detail page.'
        )
      } finally {
        setIsUploading(false)
      }
    } else {
      toast.success('Contract renewed successfully.')
    }

    router.push(`/contracts/${result.id}`)
  }

  if (loadingContract || loadingEligibility) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
        <Skeleton className="h-64 w-full" />
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
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="sm" asChild>
          <Link href={`/contracts/${contractId}`}>
            <ArrowLeft className="h-4 w-4" />
            Back
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            Renew Contract — <span className="font-mono">{contract.contractNumber}</span>
          </h1>
          <p className="text-sm text-muted-foreground">
            Current end date: {format(new Date(contract.endDate), 'MMM d, yyyy')}
          </p>
        </div>
      </div>

      {/* Eligibility gate */}
      {isEligible === false && (
        <EligibilityGate
          collaboratorId={contract.collaboratorId}
          averageRating={avgRating?.averageRating ?? null}
        />
      )}

      {/* Renewal form — only shown when eligible */}
      {isEligible === true && (
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Renewal Details</CardTitle>
                <CardDescription>
                  Fields left blank will carry over from the current contract.
                </CardDescription>
              </CardHeader>

              <CardContent className="space-y-6">
                {/* New end date */}
                <FormField
                  control={form.control}
                  name="newEndDate"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>New End Date *</FormLabel>
                      <FormControl>
                        <Input type="date" min={contract.endDate} {...field} />
                      </FormControl>
                      <FormDescription>Must be after the current end date.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Separator />

                {/* Salary + settings */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <FormField
                    control={form.control}
                    name="newSalary"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>New Salary</FormLabel>
                        <FormControl>
                          <Input
                            type="number"
                            min={0}
                            step={0.01}
                            placeholder={contract.salary ? String(contract.salary) : '0.00'}
                            {...field}
                            value={field.value ?? ''}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="noticePeriodDays"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Notice Period (days)</FormLabel>
                        <FormControl>
                          <Input type="number" min={0} {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <FormField
                  control={form.control}
                  name="autoRenewal"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-center justify-between rounded-lg border p-3">
                      <div>
                        <FormLabel>Auto Renewal</FormLabel>
                        <FormDescription>Automatically renew when this contract expires</FormDescription>
                      </div>
                      <FormControl>
                        <Switch checked={field.value} onCheckedChange={field.onChange} />
                      </FormControl>
                    </FormItem>
                  )}
                />

                <Separator />

                <FormField
                  control={form.control}
                  name="renewalNotes"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Renewal Notes</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Any notes about this renewal…"
                          className="min-h-[80px]"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>Optional. Max 1,000 characters.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="newTermsAndConditions"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>New Terms and Conditions</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Leave blank to keep existing terms…"
                          className="min-h-[120px] resize-y"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>Optional. Max 10,000 characters.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Separator />

                {/* File upload */}
                <div className="space-y-2">
                  <FormLabel>Renewal Document</FormLabel>
                  <FileDropzone file={file} onFileChange={setFile} />
                </div>
              </CardContent>
            </Card>

            <div className="flex items-center justify-between">
              <Button type="button" variant="outline" asChild>
                <Link href={`/contracts/${contractId}`}>Cancel</Link>
              </Button>
              <Button type="submit" disabled={isSubmitting} className="min-w-[160px]">
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {isUploading ? 'Uploading…' : 'Renewing…'}
                  </>
                ) : (
                  'Renew Contract'
                )}
              </Button>
            </div>
          </form>
        </Form>
      )}
    </div>
  )
}
