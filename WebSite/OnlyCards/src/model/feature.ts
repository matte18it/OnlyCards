export class Feature {
    name: string;
    value: string;

    constructor(name: string, value: string) {
        this.name = name;
        this.value = value;
    }
}
export class FeatureSearch {
  name: string;
  value: string[];
  constructor(obj:any) {
    this.name = obj.name;
    this.value= obj.value;
  }
}
