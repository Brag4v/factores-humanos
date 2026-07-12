import { CollaboratorDetailView } from '@/components/collaborators/CollaboratorDetailView'

interface CollaboratorDetailPageProps {
  params: { nationalId: string }
}

export default function CollaboratorDetailPage({ params }: CollaboratorDetailPageProps) {
  return <CollaboratorDetailView nationalId={params.nationalId} />
}
