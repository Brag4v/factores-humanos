import { z } from 'zod'

export const collaboratorSchema = z.object({
  nationalId: z.string().min(1, 'Required').max(20, 'Max 20 characters'),
  firstName: z.string().min(1, 'Required').max(100, 'Max 100 characters'),
  lastName: z.string().min(1, 'Required').max(100, 'Max 100 characters'),
  email: z.string().min(1, 'Required').email('Must be a valid email').max(255),
  phone: z.string().max(20, 'Max 20 characters').optional().or(z.literal('')),
  department: z.string().max(100, 'Max 100 characters').optional().or(z.literal('')),
  position: z.string().min(1, 'Required').max(100, 'Max 100 characters'),
  status: z.enum(['ACTIVE', 'INACTIVE', 'ON_HOLD']).default('ACTIVE'),
  hireDate: z.string().min(1, 'Required'),
})

export type CollaboratorFormValues = z.infer<typeof collaboratorSchema>
