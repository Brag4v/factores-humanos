import { EditCollaboratorForm } from '@/components/collaborators/EditCollaboratorForm'

interface EditCollaboratorPageProps {
  params: { nationalId: string }
}

export default function EditCollaboratorPage({ params }: EditCollaboratorPageProps) {
  return (
    <div className="space-y-6 p-6 max-w-2xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Edit Collaborator</h1>
        <p className="text-sm text-muted-foreground">Update the employee's details below.</p>
      </div>
      <EditCollaboratorForm nationalId={params.nationalId} />
    </div>
  )
}
