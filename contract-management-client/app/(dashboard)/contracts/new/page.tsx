import { Suspense } from 'react'
import type { Metadata } from 'next'
import { CreateContractForm } from '@/components/contracts/CreateContractForm'

export const metadata: Metadata = {
  title: 'New Contract',
  description: 'Create a new contract.',
}

export default function NewContractPage() {
  return (
    <div className="space-y-6 p-6 max-w-2xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">New Contract</h1>
        <p className="text-sm text-muted-foreground">Fill in the details to create a new contract.</p>
      </div>
      <Suspense>
        <CreateContractForm />
      </Suspense>
    </div>
  )
}
