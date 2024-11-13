export class AdvancedSearchProductType {
    id : string;
    name: string;
    image: string;
    setName: string;
    collectorNumber: string;
    rarity: string;
    type: string;

    constructor(id: string, name: string, image: string, setName: string, collectorNumber: string, rarity: string, type: string) {
      this.id = id;
      this.name = name;
      this.image = image;
      this.setName = setName;
      this.collectorNumber = collectorNumber;
      this.rarity = rarity;
      this.type = type;
    }
}
