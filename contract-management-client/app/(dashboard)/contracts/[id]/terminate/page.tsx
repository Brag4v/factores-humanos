import { TerminateContractForm } from '@/components/contracts/TerminateContractForm'

interface TerminateContractPageProps {
  params: { id: string }
}

export default function TerminateContractPage({ params }: TerminateContractPageProps) {
  return (
    <div className="p-6 max-w-2xl mx-auto">
      <TerminateContractForm contractId={params.id} />
    </div>
  )
}
