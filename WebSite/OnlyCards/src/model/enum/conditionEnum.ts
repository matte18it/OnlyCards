/* ENUM RELATIVO ALLE CONDIZIONI */
export enum Condition {
  MINT = "MINT",  // mint
  NEAR_MINT = "NEAR_MINT",  // near mint
  EXCELLENT = "EXCELLENT",  // excellent
  GOOD = "GOOD",  // good
  LIGHT_PLAYED = "LIGHT_PLAYED",  // light played
  PLAYED = "PLAYED",  // played
  POOR = "POOR"  // played
}

export function getConditionArray(): string[] {
    return [
        getConditionString(Condition.MINT),
        getConditionString(Condition.NEAR_MINT),
        getConditionString(Condition.EXCELLENT),
        getConditionString(Condition.GOOD),
        getConditionString(Condition.LIGHT_PLAYED),
        getConditionString(Condition.PLAYED),
        getConditionString(Condition.POOR)
    ];
}

export function getConditionString(value: Condition): string {
  switch (value) {
    case Condition.MINT:
      return 'Mint';
    case Condition.NEAR_MINT:
      return 'Near Mint';
    case Condition.EXCELLENT:
      return 'Excellent';
    case Condition.GOOD:
      return 'Good';
    case Condition.LIGHT_PLAYED:
      return 'Light Played';
    case Condition.PLAYED:
      return 'Played';
    case Condition.POOR:
      return 'Poor';
    default:
      return '-';
  }}

  export function getConditionFromString(value: string): Condition {
    switch (value) {
      case 'Mint':
        return Condition.MINT;
      case 'Near Mint':
        return Condition.NEAR_MINT;
      case 'Excellent':
        return Condition.EXCELLENT;
      case 'Good':
        return Condition.GOOD;
      case 'Light Played':
        return Condition.LIGHT_PLAYED;
      case 'Played':
        return Condition.PLAYED;
      case 'Poor':
        return Condition.POOR;
      default:
        return Condition.MINT;
    }
  }


