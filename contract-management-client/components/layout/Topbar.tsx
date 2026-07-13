import { Bell } from 'lucide-react'
import { Button } from '@/components/ui/button'

export function Topbar() {
  return (
    <header className="flex h-16 flex-shrink-0 items-center justify-between border-b bg-white px-6">
      <div className="flex items-center gap-2">
        {/* Breadcrumbs injected per-page via page titles */}
      </div>

      <div className="flex items-center gap-2">
        <Button variant="ghost" size="icon" className="text-slate-500 hover:text-slate-900" aria-label="Notifications">
          <Bell className="h-4 w-4" />
        </Button>

        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-600 text-xs font-semibold text-white">
          HR
        </div>
      </div>
    </header>
  )
}
