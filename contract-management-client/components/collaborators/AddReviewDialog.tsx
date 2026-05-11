'use client'

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { PlusCircle } from 'lucide-react'
import { toast } from 'sonner'
import { reviewSchema, type ReviewFormValues } from '@/lib/schemas/review.schema'
import { useCreateReview } from '@/lib/hooks/useCollaborators'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { PERFORMANCE_CATEGORY_LABELS } from '@/constants'
import type { PerformanceCategory } from '@/lib/types'
import { useState } from 'react'

const CATEGORY_OPTIONS: PerformanceCategory[] = [
  'EXCEEDS_EXPECTATIONS',
  'MEETS_EXPECTATIONS',
  'BELOW_EXPECTATIONS',
  'NEEDS_IMPROVEMENT',
]

interface AddReviewDialogProps {
  collaboratorId: string
}

export function AddReviewDialog({ collaboratorId }: AddReviewDialogProps) {
  const [open, setOpen] = useState(false)
  const createReview = useCreateReview()

  const form = useForm<ReviewFormValues>({
    resolver: zodResolver(reviewSchema),
    defaultValues: {
      reviewerName: '',
      reviewerEmail: '',
      reviewPeriodStart: '',
      reviewPeriodEnd: '',
      rating: undefined as unknown as number,
      performanceCategory: undefined as unknown as PerformanceCategory,
      strengths: '',
      areasForImprovement: '',
      comments: '',
    },
  })

  async function onSubmit(values: ReviewFormValues) {
    await createReview.mutateAsync(
      {
        collaboratorId,
        reviewerName: values.reviewerName,
        reviewerEmail: values.reviewerEmail || undefined,
        reviewPeriodStart: values.reviewPeriodStart,
        reviewPeriodEnd: values.reviewPeriodEnd,
        rating: values.rating,
        performanceCategory: values.performanceCategory,
        strengths: values.strengths || undefined,
        areasForImprovement: values.areasForImprovement || undefined,
        comments: values.comments || undefined,
      },
      {
        onSuccess: () => {
          toast.success('Performance review added.')
          form.reset()
          setOpen(false)
        },
        onError: (err: unknown) => {
          toast.error((err as Error)?.message ?? 'Failed to save the review. Please try again.')
        },
      }
    )
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <PlusCircle className="h-4 w-4" />
          Add Review
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>New Performance Review</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5 pt-2">
            {/* Reviewer */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="reviewerName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Reviewer Name *</FormLabel>
                    <FormControl>
                      <Input placeholder="Jane Smith" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="reviewerEmail"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Reviewer Email</FormLabel>
                    <FormControl>
                      <Input type="email" placeholder="jane@company.com" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Separator />

            {/* Period & Rating */}
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="reviewPeriodStart"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Period Start *</FormLabel>
                    <FormControl>
                      <Input type="date" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="reviewPeriodEnd"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Period End *</FormLabel>
                    <FormControl>
                      <Input type="date" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="rating"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Rating (1 – 5) *</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        min={1}
                        max={5}
                        step={0.01}
                        placeholder="3.50"
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
                name="performanceCategory"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Performance Category *</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select category" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {CATEGORY_OPTIONS.map((c) => (
                          <SelectItem key={c} value={c}>
                            {PERFORMANCE_CATEGORY_LABELS[c]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Separator />

            {/* Comments */}
            <FormField
              control={form.control}
              name="strengths"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Strengths</FormLabel>
                  <FormControl>
                    <Textarea placeholder="Key strengths observed…" className="min-h-[80px]" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="areasForImprovement"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Areas for Improvement</FormLabel>
                  <FormControl>
                    <Textarea placeholder="Areas to work on…" className="min-h-[80px]" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="comments"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Comments</FormLabel>
                  <FormControl>
                    <Textarea placeholder="Additional notes…" className="min-h-[80px]" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-3 pt-2">
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={createReview.isPending}>
                {createReview.isPending ? 'Saving…' : 'Save Review'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
