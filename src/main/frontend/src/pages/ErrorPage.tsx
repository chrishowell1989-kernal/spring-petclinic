import Typography from '@mui/material/Typography'
import { useTranslation } from 'react-i18next'

interface ErrorPageProps {
  status?: number
  message?: string
}

export default function ErrorPage({ status, message }: ErrorPageProps) {
  const { t } = useTranslation()

  const detail =
    message ??
    (status === 404
      ? t('error.404')
      : status === 500
        ? t('error.500')
        : t('error.general'))

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('error.title')}
      </Typography>
      <Typography>{detail}</Typography>
    </div>
  )
}
