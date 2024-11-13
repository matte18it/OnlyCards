// export class Address {
//   id?: string;
//   street?: string;
//   city?: string;
//   zip?: string;
//   state?: string;
//   defaultAddress?: boolean;
//   weekendDelivery?: boolean;
//   name?: string;
//   surname?: string;
//   telephoneNumber?: string;
//
//   constructor(obj: any) {
//     this.id = obj.id;
//     this.street = obj.street;
//     this.city = obj.city;
//     this.zip = obj.zip;
//     this.state = obj.state;
//     this.defaultAddress = obj.defaultAddress;
//     this.weekendDelivery = obj.weekendDelivery;
//     this.name = obj.name;
//     this.surname = obj.surname;
//     this.telephoneNumber = obj.telephoneNumber;
//   }
// }

export class Address {
  id?: string;
  street?: string;
  city?: string;
  zip?: string;
  state?: string;
  defaultAddress?: boolean;
  weekendDelivery?: boolean;
  name?: string;
  surname?: string;
  telephoneNumber?: string;

  constructor(obj: Partial<Address> = {}) {
    this.id = obj.id ?? '';
    this.street = obj.street ?? '';
    this.city = obj.city ?? '';
    this.zip = obj.zip ?? '';
    this.state = obj.state ?? '';
    this.defaultAddress = obj.defaultAddress ?? false;
    this.weekendDelivery = obj.weekendDelivery ?? false;
    this.name = obj.name ?? '';
    this.surname = obj.surname ?? '';
    this.telephoneNumber = obj.telephoneNumber ?? '';
  }
}

