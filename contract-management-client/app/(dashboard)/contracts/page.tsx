import { Suspense } from 'react'
import type { Metadata } from 'next'
import { ContractsView } from '@/components/contracts/ContractsView'

export const metadata: Metadata = {
  title: 'Contracts',
  description: 'Browse, search, and filter contracts.',
}

export default function ContractsPage() {
  return (
    <Suspense>
      <ContractsView />
    </Suspense>
  )
}
