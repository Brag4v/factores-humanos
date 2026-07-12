import type { Metadata } from 'next'
import { NewCollaboratorFlow } from '@/components/collaborators/NewCollaboratorFlow'

export const metadata: Metadata = {
  title: 'Add Collaborator',
  description: 'Register a new collaborator.',
}

export default function NewCollaboratorPage() {
  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Add Collaborator</h1>
        <p className="text-sm text-muted-foreground">Complete the steps below to register a new collaborator.</p>
      </div>
      <NewCollaboratorFlow />
    </div>
  )
}
