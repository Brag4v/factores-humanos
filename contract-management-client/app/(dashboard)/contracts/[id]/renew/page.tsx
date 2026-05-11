import { RenewContractForm } from '@/components/contracts/RenewContractForm'

interface RenewContractPageProps {
  params: { id: string }
}

export default function RenewContractPage({ params }: RenewContractPageProps) {
  return (
    <div className="p-6 max-w-2xl mx-auto">
      <RenewContractForm contractId={params.id} />
    </div>
  )
}
