export class ProductImage{
    id: string;
    photo: string;

    constructor(obj: any){
        this.id = obj.id;
        this.photo = obj.photo;
    }
}
export class ProductImageEdit{
    id: string | null;
    photo: File;

    constructor(photo: File, id?: string){
        this.photo = photo;
        this.id = null;
    }
}
