import Link from 'next/link'
import { UserPlus, FileText } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export function QuickActions() {
  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle>Quick Actions</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-wrap gap-3">
        <Button asChild>
          <Link href="/collaborators/new">
            <UserPlus className="h-4 w-4" />
            Add Collaborator
          </Link>
        </Button>
        <Button variant="outline" asChild>
          <Link href="/contracts/new">
            <FileText className="h-4 w-4" />
            New Contract
          </Link>
        </Button>
      </CardContent>
    </Card>
  )
}
