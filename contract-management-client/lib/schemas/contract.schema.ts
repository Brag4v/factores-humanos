import { z } from 'zod'

export const contractSchema = z
  .object({
    contractType: z.enum(['FULL_TIME', 'PART_TIME', 'CONTRACTOR', 'TEMPORARY'], {
      required_error: 'Contract type is required',
    }),
    startDate: z.string().min(1, 'Start date is required'),
    endDate: z.string().min(1, 'End date is required'),
    salary: z.coerce.number().min(0, 'Must be ≥ 0').optional().or(z.literal(0)),
    currency: z.string().max(3).default('USD'),
    noticePeriodDays: z.coerce.number().min(0, 'Must be ≥ 0').default(30),
    autoRenewal: z.boolean().default(false),
    termsAndConditions: z.string().max(10000, 'Max 10,000 characters').optional().or(z.literal('')),
  })
  .refine((data) => !data.startDate || !data.endDate || new Date(data.endDate) > new Date(data.startDate), {
    message: 'End date must be after start date',
    path: ['endDate'],
  })

export type ContractFormValues = z.infer<typeof contractSchema>

export const renewalSchema = z.object({
  newEndDate: z.string().min(1, 'New end date is required'),
  newSalary: z.coerce.number().min(0, 'Must be ≥ 0').optional().or(z.literal(0)),
  newTermsAndConditions: z.string().max(10000).optional().or(z.literal('')),
  autoRenewal: z.boolean().default(false),
  noticePeriodDays: z.coerce.number().min(0).default(30),
  renewalNotes: z.string().max(1000).optional().or(z.literal('')),
})

export type RenewalFormValues = z.infer<typeof renewalSchema>

export const terminationSchema = z.object({
  reason: z.string().min(1, 'Reason is required').max(1000, 'Max 1,000 characters'),
  effectiveDate: z.string().min(1, 'Effective date is required'),
  additionalNotes: z.string().max(2000).optional().or(z.literal('')),
})

export type TerminationFormValues = z.infer<typeof terminationSchema>
