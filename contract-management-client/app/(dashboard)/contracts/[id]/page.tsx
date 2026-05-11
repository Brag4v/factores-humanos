import { ContractDetailView } from '@/components/contracts/ContractDetailView'

interface ContractDetailPageProps {
  params: { id: string }
}

export default function ContractDetailPage({ params }: ContractDetailPageProps) {
  return <ContractDetailView id={params.id} />
}
