import { z } from 'zod'

export const reviewSchema = z
  .object({
    reviewerName: z.string().min(1, 'Required').max(200, 'Max 200 characters'),
    reviewerEmail: z.string().email('Must be a valid email').max(255).optional().or(z.literal('')),
    reviewPeriodStart: z.string().min(1, 'Required'),
    reviewPeriodEnd: z.string().min(1, 'Required'),
    rating: z.coerce.number().min(1, 'Min 1.00').max(5, 'Max 5.00'),
    performanceCategory: z.enum(
      ['EXCEEDS_EXPECTATIONS', 'MEETS_EXPECTATIONS', 'BELOW_EXPECTATIONS', 'NEEDS_IMPROVEMENT'],
      { required_error: 'Category is required' }
    ),
    strengths: z.string().max(5000, 'Max 5,000 characters').optional().or(z.literal('')),
    areasForImprovement: z.string().max(5000, 'Max 5,000 characters').optional().or(z.literal('')),
    comments: z.string().max(5000, 'Max 5,000 characters').optional().or(z.literal('')),
  })
  .refine(
    (data) =>
      !data.reviewPeriodStart ||
      !data.reviewPeriodEnd ||
      new Date(data.reviewPeriodEnd) >= new Date(data.reviewPeriodStart),
    { message: 'End date must be on or after start date', path: ['reviewPeriodEnd'] }
  )

export type ReviewFormValues = z.infer<typeof reviewSchema>
