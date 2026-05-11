'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Save } from 'lucide-react'
import { collaboratorSchema, type CollaboratorFormValues } from '@/lib/schemas/collaborator.schema'
import { useCollaborator, useUpdateCollaborator } from '@/lib/hooks/useCollaborators'
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { COLLABORATOR_STATUS_LABELS } from '@/constants'
import type { CollaboratorStatus } from '@/lib/types'

const STATUS_OPTIONS: CollaboratorStatus[] = ['ACTIVE', 'INACTIVE', 'ON_HOLD']

interface EditCollaboratorFormProps {
  nationalId: string
}

export function EditCollaboratorForm({ nationalId }: EditCollaboratorFormProps) {
  const router = useRouter()
  const { data: collaborator, isLoading } = useCollaborator(nationalId)
  const updateCollaborator = useUpdateCollaborator(nationalId)

  const today = new Date().toISOString().split('T')[0]

  const form = useForm<CollaboratorFormValues>({
    resolver: zodResolver(collaboratorSchema),
    defaultValues: {
      nationalId: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      department: '',
      position: '',
      status: 'ACTIVE',
      hireDate: '',
    },
  })

  useEffect(() => {
    if (collaborator) {
      form.reset({
        nationalId: collaborator.nationalId,
        firstName: collaborator.firstName,
        lastName: collaborator.lastName,
        email: collaborator.email,
        phone: collaborator.phone ?? '',
        department: collaborator.department ?? '',
        position: collaborator.position,
        status: collaborator.status,
        hireDate: collaborator.hireDate,
      })
    }
  }, [collaborator, form])

  async function onSubmit(values: CollaboratorFormValues) {
    await updateCollaborator.mutateAsync(
      {
        nationalId: values.nationalId,
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        phone: values.phone || undefined,
        department: values.department || undefined,
        position: values.position,
        status: values.status,
        hireDate: values.hireDate,
      },
      {
        onSuccess: () => {
          toast.success('Collaborator updated successfully.')
          router.push(`/collaborators/${nationalId}`)
        },
        onError: (err: unknown) => {
          toast.error((err as Error)?.message ?? 'Failed to save changes. Please try again.')
        },
      }
    )
  }

  if (isLoading) {
    return (
      <Card>
        <CardContent className="pt-6 space-y-4">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-10 w-full" />
          ))}
        </CardContent>
      </Card>
    )
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Collaborator Information</CardTitle>
            <CardDescription>Update the employee's personal and professional details.</CardDescription>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Identity — nationalId is read-only */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="nationalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>National ID</FormLabel>
                    <FormControl>
                      <Input {...field} readOnly className="bg-muted cursor-not-allowed" />
                    </FormControl>
                    <FormDescription>Cannot be changed after creation.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormItem>
                <FormLabel>Employee Code</FormLabel>
                <Input value={collaborator?.employeeCode ?? ''} readOnly className="bg-muted cursor-not-allowed" />
                <FormDescription>Auto-assigned, cannot be changed.</FormDescription>
              </FormItem>
            </div>

            <Separator />

            {/* Name */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="firstName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>First Name *</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="lastName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Last Name *</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {/* Contact */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email *</FormLabel>
                    <FormControl>
                      <Input type="email" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="phone"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Phone</FormLabel>
                    <FormControl>
                      <Input placeholder="+1 555 000 0000" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Separator />

            {/* Work */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="department"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Department</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g. Engineering" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="position"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Position *</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g. Software Engineer" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="hireDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Hire Date *</FormLabel>
                    <FormControl>
                      <Input type="date" max={today} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="status"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Status</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select status" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {STATUS_OPTIONS.map((s) => (
                          <SelectItem key={s} value={s}>
                            {COLLABORATOR_STATUS_LABELS[s]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </CardContent>
        </Card>

        <div className="flex items-center justify-between">
          <Button
            type="button"
            variant="outline"
            onClick={() => router.push(`/collaborators/${nationalId}`)}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={updateCollaborator.isPending} className="min-w-[140px]">
            <Save className="h-4 w-4" />
            {updateCollaborator.isPending ? 'Saving…' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </Form>
  )
}
