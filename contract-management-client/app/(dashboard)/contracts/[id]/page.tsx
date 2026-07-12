import type { Metadata } from 'next'
import { ContractDetailView } from '@/components/contracts/ContractDetailView'

interface ContractDetailPageProps {
  params: { id: string }
}

export const metadata: Metadata = {
  title: 'Contract Details',
  description: 'Contract information, documents, and notification history.',
}

export default function ContractDetailPage({ params }: ContractDetailPageProps) {
  return <ContractDetailView id={params.id} />
}
