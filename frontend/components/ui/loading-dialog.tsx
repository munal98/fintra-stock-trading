import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogDescription,
} from "@/components/ui/alert-dialog"
import { Loader2 } from "lucide-react"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

export function LoadingDialog({ isOpen }: { isOpen: boolean }) {
    const { t } = useTranslation()
    return (
        <AlertDialog open={isOpen}>
            <AlertDialogContent
                className="flex max-w-xs flex-col items-center justify-center gap-4 text-center p-6"
            >
                <AlertDialogHeader>
                    <AlertDialogTitle className="text-base flex flex-col justify-center text-center items-center gap-2">Loading...</AlertDialogTitle>
                    <AlertDialogDescription className="text-sm text-muted-foreground">
                        {t("Please wait until the process is completed.")}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <Loader2 className="h-14 w-14 animate-spin text-primary" />
            </AlertDialogContent>
        </AlertDialog>
    )
}
