'use client'

import { useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { contractSchema } from '@/lib/schemas/contract.schema'
import { useCreateContract } from '@/lib/hooks/useContracts'
import { useCollaborators } from '@/lib/hooks/useCollaborators'
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { FileDropzone } from '@/components/shared/FileDropzone'
import { apiClient } from '@/lib/api/client'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import type { ContractType } from '@/lib/types'

const CONTRACT_TYPE_OPTIONS: ContractType[] = ['FULL_TIME', 'PART_TIME', 'CONTRACTOR', 'TEMPORARY']

const createContractSchema = contractSchema.and(z.object({ collaboratorId: z.string().min(1, 'Required') }))
type CreateContractFormValues = z.infer<typeof createContractSchema>

export function CreateContractForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const prefillId = searchParams.get('collaboratorId') ?? ''
  const isLocked = !!prefillId

  const [file, setFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)

  const createContract = useCreateContract()
  const { data: collaboratorsPage, isLoading: loadingCollaborators } = useCollaborators({ size: 200, status: 'ACTIVE' })
  const collaborators = collaboratorsPage?.content ?? []

  const form = useForm<CreateContractFormValues>({
    resolver: zodResolver(createContractSchema),
    defaultValues: {
      collaboratorId: prefillId,
      contractType: undefined,
      startDate: '',
      endDate: '',
      salary: undefined,
      currency: 'USD',
      noticePeriodDays: 30,
      autoRenewal: false,
      termsAndConditions: '',
    },
  })

  const selectedId = form.watch('collaboratorId')
  const selectedCollaborator = collaborators.find((c) => c.nationalId === selectedId)

  async function onSubmit(values: CreateContractFormValues) {
    try {
      const contract = await createContract.mutateAsync({
        collaboratorId: values.collaboratorId,
        contractType: values.contractType,
        startDate: values.startDate,
        endDate: values.endDate,
        salary: values.salary || undefined,
        currency: values.currency,
        noticePeriodDays: values.noticePeriodDays,
        autoRenewal: values.autoRenewal,
        termsAndConditions: values.termsAndConditions || undefined,
      })

      if (file) {
        setIsUploading(true)
        try {
          const fd = new FormData()
          fd.append('file', file)
          await apiClient.post(`/api/v1/contracts/${contract.id}/documents`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
          toast.success('Contract and document created successfully.')
        } catch {
          toast.warning('Contract created, but document upload failed. Upload it from the contract detail page.')
        } finally {
          setIsUploading(false)
        }
      } else {
        toast.success('Contract created successfully.')
      }

      router.push(`/contracts/${contract.id}`)
    } catch (err) {
      const msg = (err as Error)?.message ?? 'Failed to create contract. Please try again.'
      if (msg.toLowerCase().includes('active contract')) {
        form.setError('collaboratorId', { message: 'This collaborator already has an active contract' })
      } else {
        toast.error(msg)
      }
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Contract Details</CardTitle>
            {selectedCollaborator && (
              <CardDescription>
                Creating contract for <strong>{selectedCollaborator.fullName}</strong>
                <span className="text-muted-foreground"> · {selectedCollaborator.department} · {selectedCollaborator.position}</span>
              </CardDescription>
            )}
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Collaborator */}
            <FormField
              control={form.control}
              name="collaboratorId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Collaborator *</FormLabel>
                  {loadingCollaborators ? (
                    <Skeleton className="h-10 w-full" />
                  ) : isLocked ? (
                    <>
                      <FormControl>
                        <Input
                          value={selectedCollaborator?.fullName ?? prefillId}
                          readOnly
                          className="bg-muted cursor-not-allowed"
                        />
                      </FormControl>
                      <FormDescription>Pre-filled — cannot be changed.</FormDescription>
                    </>
                  ) : (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a collaborator…" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {collaborators.map((c) => (
                          <SelectItem key={c.nationalId} value={c.nationalId}>
                            <span className="font-medium">{c.fullName}</span>
                            <span className="ml-2 text-muted-foreground text-xs">{c.department} · {c.position}</span>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                  <FormMessage />
                </FormItem>
              )}
            />

            <Separator />

            {/* Type */}
            <FormField
              control={form.control}
              name="contractType"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Contract Type *</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select contract type" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {CONTRACT_TYPE_OPTIONS.map((t) => (
                        <SelectItem key={t} value={t}>
                          {CONTRACT_TYPE_LABELS[t]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Separator />

            {/* Dates */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="startDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Start Date *</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="endDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>End Date *</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Separator />

            {/* Compensation */}
            <div className="grid gap-4 sm:grid-cols-3">
              <div className="sm:col-span-2">
                <FormField
                  control={form.control}
                  name="salary"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Salary</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          min={0}
                          step={0.01}
                          placeholder="5000.00"
                          {...field}
                          value={field.value ?? ''}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <FormField
                control={form.control}
                name="currency"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Currency</FormLabel>
                    <FormControl><Input placeholder="USD" maxLength={3} {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Separator />

            {/* Settings */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="noticePeriodDays"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Notice Period (days)</FormLabel>
                    <FormControl><Input type="number" min={0} {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="autoRenewal"
                render={({ field }) => (
                  <FormItem className="flex flex-row items-center justify-between rounded-lg border p-3">
                    <div>
                      <FormLabel>Auto Renewal</FormLabel>
                      <FormDescription>Automatically renew when contract expires</FormDescription>
                    </div>
                    <FormControl>
                      <Switch checked={field.value} onCheckedChange={field.onChange} />
                    </FormControl>
                  </FormItem>
                )}
              />
            </div>

            {/* Terms */}
            <FormField
              control={form.control}
              name="termsAndConditions"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Terms and Conditions</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="Enter contract terms and conditions…"
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

            {/* Document */}
            <div className="space-y-2">
              <p className="text-sm font-medium">Contract Document</p>
              <FileDropzone file={file} onFileChange={setFile} />
              <p className="text-xs text-muted-foreground">Optional. PDF, DOCX or image, max 10 MB.</p>
            </div>
          </CardContent>
        </Card>

        <div className="flex items-center justify-between">
          <Button type="button" variant="outline" onClick={() => router.back()}>
            Cancel
          </Button>
          <Button type="submit" disabled={createContract.isPending || isUploading} className="min-w-[160px]">
            {isUploading ? 'Uploading…' : createContract.isPending ? 'Creating…' : 'Create Contract'}
          </Button>
        </div>
      </form>
    </Form>
  )
}
