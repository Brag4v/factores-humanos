import type { Metadata } from 'next'
import { TerminateContractForm } from '@/components/contracts/TerminateContractForm'

interface TerminateContractPageProps {
  params: { id: string }
}

export const metadata: Metadata = {
  title: 'Terminate Contract',
  description: 'Terminate an existing contract.',
}

export default function TerminateContractPage({ params }: TerminateContractPageProps) {
  return (
    <div className="p-6 max-w-2xl mx-auto">
      <TerminateContractForm contractId={params.id} />
    </div>
  )
}
