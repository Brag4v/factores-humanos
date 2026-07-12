import type { Metadata } from 'next'
import { CollaboratorDetailView } from '@/components/collaborators/CollaboratorDetailView'

interface CollaboratorDetailPageProps {
  params: { nationalId: string }
}

export const metadata: Metadata = {
  title: 'Collaborator Details',
  description: 'Collaborator profile, contracts, and performance reviews.',
}

export default function CollaboratorDetailPage({ params }: CollaboratorDetailPageProps) {
  return <CollaboratorDetailView nationalId={params.nationalId} />
}
