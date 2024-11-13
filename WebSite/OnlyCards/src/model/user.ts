import { Address } from "./address";

export class User {
  id: string;
  email: string;
  username: string;
  cellphoneNumber?: string;
  vatNumber?: string;
  addresses?: Address[];
  oauthUser: boolean;
  blocked: boolean;
  roles: string[]; // Assicurati che i ruoli siano un array di stringhe con i ruoli esistenti

  constructor(obj?: Partial<User>) {
    this.id = obj?.id || '';
    this.email = obj?.email || '';
    this.username = obj?.username || '';
    this.cellphoneNumber = obj?.cellphoneNumber || '';
    this.vatNumber = obj?.vatNumber || '';
    this.addresses = obj?.addresses?.map((address: any) => new Address(address)) || [];
    this.blocked = obj?.blocked || false;
    this.oauthUser = obj?.oauthUser || false;
    this.roles = obj?.roles || []; // Inizializza l'array dei ruoli
  }
}


export interface UserPublic {
  username: string;
  profileImage: string;
}
