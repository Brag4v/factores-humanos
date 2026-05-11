'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, ArrowRight, SkipForward } from 'lucide-react'
import { contractSchema, type ContractFormValues } from '@/lib/schemas/contract.schema'
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { FileDropzone } from '@/components/shared/FileDropzone'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import type { ContractType } from '@/lib/types'

const CONTRACT_TYPE_OPTIONS: ContractType[] = ['FULL_TIME', 'PART_TIME', 'CONTRACTOR', 'TEMPORARY']

interface Step2Props {
  defaultValues?: Partial<ContractFormValues>
  defaultFile?: File | null
  onNext: (data: ContractFormValues, file: File | null) => void
  onBack: () => void
  onSkip: () => void
}

export function Step2ContractDetails({ defaultValues, defaultFile = null, onNext, onBack, onSkip }: Step2Props) {
  const [file, setFile] = useState<File | null>(defaultFile)

  const form = useForm<ContractFormValues>({
    resolver: zodResolver(contractSchema),
    defaultValues: {
      contractType: undefined,
      startDate: '',
      endDate: '',
      salary: undefined,
      currency: 'USD',
      noticePeriodDays: 30,
      autoRenewal: false,
      termsAndConditions: '',
      ...defaultValues,
    },
  })

  return (
    <Form {...form}>
      <Card>
        <CardHeader>
          <CardTitle>Contract Details</CardTitle>
          <CardDescription>
            Set up the initial contract for this collaborator. You can skip this and add a contract later.
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
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
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
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
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
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
                  <FormControl>
                    <Input placeholder="USD" maxLength={3} {...field} />
                  </FormControl>
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
                  <FormControl>
                    <Input type="number" min={0} {...field} />
                  </FormControl>
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

      {/* Actions */}
      <div className="flex items-center justify-between">
        <Button type="button" variant="outline" onClick={onBack}>
          <ArrowLeft className="h-4 w-4" />
          Back
        </Button>
        <div className="flex gap-3">
          <Button type="button" variant="ghost" onClick={onSkip}>
            <SkipForward className="h-4 w-4" />
            Skip contract
          </Button>
          <Button type="button" onClick={form.handleSubmit((data) => onNext(data, file))}>
            Next: Review
            <ArrowRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </Form>
  )
}
