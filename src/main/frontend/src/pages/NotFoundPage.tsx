import Typography from '@mui/material/Typography'
import { useTranslation } from 'react-i18next'

export default function NotFoundPage() {
  const { t } = useTranslation()

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('notFound.title')}
      </Typography>
      <Typography>{t('notFound.message')}</Typography>
    </div>
  )
}
