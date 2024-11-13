export enum Status {
  PENDING = "In elaborazione",  // pending
  SHIPPED = "Spedito",  // shipped
  DELIVERED = "Consegnato",  // delivered
  CANCELLED = "Cancellato"  // cancelled
}

// Funzioni per la gestione dello Status
export function getStatusString(value: Status): string {
  switch (value) {
    case Status.PENDING:
      return 'Pending';
    case Status.SHIPPED:
      return 'Shipped';
    case Status.DELIVERED:
      return 'Delivered';
    case Status.CANCELLED:
      return 'Cancelled';
    default:
      return '-';
  }
}
export function getStatusEnum(value: string): string {
  switch (value) {
    case 'In elaborazione':
      return 'PENDING';
    case 'Spedito':
      return 'SHIPPED';
    case 'Consegnato':
      return 'DELIVERED';
    case 'Cancellato':
      return 'CANCELLED';
    default:
      return '';
  }
}
export function getListStatus(): string[] {
  return Object.values(Status).filter(value => true);
}

export function getStatusValue(statusKey: string): string {
  switch (statusKey) {
    case 'PENDING':
      return Status.PENDING;
    case 'SHIPPED':
      return Status.SHIPPED;
    case 'DELIVERED':
      return Status.DELIVERED;
    case 'CANCELLED':
      return Status.CANCELLED;
    default:
      return '';
  }
}



