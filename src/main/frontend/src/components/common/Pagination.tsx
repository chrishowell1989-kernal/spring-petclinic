import Stack from '@mui/material/Stack'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import { useTranslation } from 'react-i18next'

interface PaginationProps {
  /** 0-indexed current page, matching the API's Pageable convention */
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export default function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  const { t } = useTranslation()

  if (totalPages <= 1) {
    return null
  }

  const isFirst = page === 0
  const isLast = page >= totalPages - 1

  return (
    <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mt: 2 }}>
      <Button size="small" disabled={isFirst} onClick={() => onPageChange(0)}>
        {t('pagination.first')}
      </Button>
      <Button size="small" disabled={isFirst} onClick={() => onPageChange(page - 1)}>
        {t('pagination.previous')}
      </Button>
      <Typography variant="body2" sx={{ px: 1 }}>
        {page + 1} / {totalPages}
      </Typography>
      <Button size="small" disabled={isLast} onClick={() => onPageChange(page + 1)}>
        {t('pagination.next')}
      </Button>
      <Button size="small" disabled={isLast} onClick={() => onPageChange(totalPages - 1)}>
        {t('pagination.last')}
      </Button>
    </Stack>
  )
}
