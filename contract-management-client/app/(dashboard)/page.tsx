import { Suspense } from 'react'
import type { Metadata } from 'next'
import { DashboardView } from '@/components/dashboard/DashboardView'

export const metadata: Metadata = {
  title: 'Dashboard',
  description: 'Contract management overview: active collaborators, active contracts, and upcoming expirations.',
}

export default function DashboardPage() {
  return (
    <Suspense>
      <DashboardView />
    </Suspense>
  )
}
