export function iconPath(icon: string) {
  const paths: Record<string, string> = {
    home: 'M3 10.5 12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-10.5Z',
    package: 'M4 7.5 12 3l8 4.5v9L12 21l-8-4.5v-9Zm4.2-2.4 8 4.6M12 12v9',
    chef: 'M8 7a4 4 0 0 1 8 0 3 3 0 1 1 1 5.83V21H7v-8.17A3 3 0 1 1 8 7Zm1 11h6',
    bell: 'M18 16H6l1.5-2V9a4.5 4.5 0 0 1 9 0v5L18 16Zm-8 3h4',
    plus: 'M12 5v14M5 12h14',
    search: 'm21 21-4.35-4.35M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15Z',
    filter: 'M4 5h16l-6 7v5l-4 2v-7L4 5Z',
    calendar: 'M7 3v4M17 3v4M4 9h20M6 5h18v17H6z',
    alert: 'M12 8v5M12 17h.01M21 19 12 4 3 19h18Z',
    trend: 'M4 17 10 11l4 4 7-8M15 7h6v6',
    clock: 'M12 6v6l4 2M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z',
    users: 'M16 21v-2a4 4 0 0 0-8 0v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    back: 'M19 12H5M12 19l-7-7 7-7',
    edit: 'M12 20h9M16.5 3.5l4 4L8 20H4v-4L16.5 3.5Z',
    trash: 'M4 7h16M10 11v6M14 11v6M6 7l1 14h10l1-14M9 7V4h6v3',
    check: 'M20 6 9 17l-5-5',
    heart: 'M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z',
    camera: 'M6 7h2l2-3h4l2 3h2a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-8a3 3 0 0 1 3-3Zm6 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z',
    barcode: 'M4 5v14M8 5v14M12 5v14M17 5v14M21 5v14',
  }
  return paths[icon] ?? paths.package
}

export function statusClass(daysLeft: number) {
  if (daysLeft < 0) return 'danger'
  if (daysLeft <= 3) return 'danger'
  if (daysLeft <= 7) return 'warning'
  return 'success'
}

export function daysLabel(daysLeft: number) {
  if (daysLeft < 0) return `${Math.abs(daysLeft)}일 지남`
  if (daysLeft === 0) return '오늘 만료'
  return `${daysLeft}일 남음`
}

export function notificationLabel(type: string) {
  return {
    expired: '만료됨',
    expiry: '임박',
    recipe: '레시피',
    inventory: '재고',
  }[type]
}
