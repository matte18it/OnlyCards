import {Component, inject, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ProductType, ProductTypeRegistration} from "../../model/productType";
import {NgbActiveModal, NgbModal, NgbTypeahead} from "@ng-bootstrap/ng-bootstrap";
import {AbstractControl, FormArray, FormBuilder, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {Feature, FeatureSearch} from "../../model/feature";
import {ProductTypeService} from "../../service/product-type/product-type.service";
import {environment} from "../../utility/environment";
import {HandlerService} from "../../service/error-handler/handler.service";
import {debounceTime, distinctUntilChanged, filter, finalize, map, merge, Observable, OperatorFunction, Subject} from "rxjs";
import {NgxSpinnerService} from "ngx-spinner";
import {GameService} from "../../service/game/game.service";
import {ActivatedRoute} from "@angular/router";
import { Title } from '@angular/platform-browser';


@Component({
  selector: 'app-admin-product-type',
  templateUrl: './admin-product-type.component.html',
  styleUrl: './admin-product-type.component.css'
})
export class AdminProductTypeComponent implements OnInit {


  @Input() productType: ProductType|null=null;
  @Input() languages!: Map<string, string>;
  @Input() features!: FeatureSearch[];
  @Input() currentGame!: string;
  protected active=1;
  protected games: Map<string, string> = new Map<string, string>();
  @Input() types!:  Map<string, string>;
  savedFeatures: boolean[]=[];
  form:FormGroup;
  form2:FormGroup;
  activeModal=inject(NgbActiveModal);
  selectedFeature: number=0;
  modify: boolean=false;
  constructor(private route:ActivatedRoute,private gameService:GameService, private title: Title, private loading:NgxSpinnerService, private handler:HandlerService, private modalService:NgbModal, private formBuilder: FormBuilder, private productTypeService: ProductTypeService) {
    this.form = this.formBuilder.group({
      name: ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
      language: ['',  Validators.compose([Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
      game:['', Validators.compose([Validators.required, Validators.minLength(3),Validators.pattern('^[a-zA-Z0-9 -àáâäãåçèéêëìíîïñòóôöõùúûüýÿÀÁÂÄÃÅÇÈÉÊËÌÍÎÏÑÒÓÔÖÕÙÚÛÜÝ]*$')])],
      type: ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
      photo: [null, Validators.compose([ this.fileSizeValidator(20)])],
      photoUrl: ['', Validators.compose([ Validators.minLength(10),   Validators.pattern('^https://.*')])],

    });
    this.form2 = this.formBuilder.group({
      features: this.formBuilder.array([]),
    });
  }




  ngOnInit(): void {
    this.title.setTitle("Only Cards | Modifica Tipo di Prodotto");
    this.loading.show();
    this.gameService.games.subscribe((response) => {
      if(response.body){
        this.games = response.body;
        if(!this.productType){
          this.form.get('game')?.setValue(this.getArray(this.games)[0]);}


      }
      this.loading.hide();

    }, (error) => {
      this.loading.hide();

    });
    if(this.productType){
      this.form.get('name')?.setValue(this.productType.name);
      this.form.get('language')?.setValue(this.productType.language);
      this.form.get('game')?.setValue(this.productType.game);
      this.form.get('type')?.setValue(this.productType.type);
      this.productType.features.forEach((feature) => {
        this.getFeaturesArray().push(this.formBuilder.group({
          name: [feature.name, Validators.compose([Validators.required, Validators.minLength(1), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
          value: [feature.value, Validators.compose([Validators.required, Validators.minLength(1), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
        }));
        this.savedFeatures.push(true);
      });
    }else {
      this.form.get('language')?.setValue(this.getLanguageValues()[0]);
      this.form.get('type')?.setValue(this.getArray(this.types)[0]);
    }


  }
  validations = {
    'name': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Nome troppo corto' }
    ], 'photo': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'fileSizeExceeded', message: 'Dimensione massima 20MB' }
    ],
    'photoUrl': [
      { type: 'pattern', message: 'Url non valido' },
      { type: 'minlength', message: 'Url troppo corto' }
    ],
    'language': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Nome troppo corto' }
    ],
    'game': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Nome troppo corto' }
    ],

    'features.name': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Nome troppo corto' },
      { type: 'sameName', message: 'Nome già presente' },
    ],
    'features.value': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Valore troppo corto' },],
    'type': [
      { type: 'required', message: 'Campo obbligatorio' },
      { type: 'pattern', message: 'Caratteri non consentiti' },
      { type: 'minlength', message: 'Nome troppo corto' }
    ],


  };

  protected readonly Object = Object;

  getLanguageValues() {
    return Array.from(Object.values(this.languages));
  }


  changeFile($event: Event) {
    const file = ($event.target as HTMLInputElement).files?.item(0);
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.form.get('photo')?.setValue(file);
      };
      reader.readAsDataURL(file);
    }

  }
  fileSizeValidator(maxSizeMB: number): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const file = control.value;
      if (file && file.size > maxSizeMB * 1024 * 1024) {
        this.form.get('photo')?.markAsDirty();
        return { 'fileSizeExceeded': true };
      }
      return null;
    };
  }

  getFeaturesArray() {
    if(!this.form2.get('features')){
      return new FormArray<any>([]);
    }
    return this.form2.get('features') as FormArray;
  }

  openModalNewFeauture(feature: TemplateRef<any>) {
    if(this.getFeaturesArray().length===10){
      this.handler.throwGlobalError('Hai raggiunto il numero massimo di caratteristiche');
      return;
    }
    this.getFeaturesArray().push(this.addNewFeature());
    this.selectedFeature = this.getFeaturesArray().length - 1;
    const  modal=this.modalService.open(feature);
    this.modify= false;
    modal.result.then(
      (reason) => {
      },
      (reason) => {
        this.getFeaturesArray().removeAt(this.selectedFeature);
        this.selectedFeature = this.selectedFeature - 1;
      }
    );

  }

  private addNewFeature() {
    return this.formBuilder.group({
      name: ['', Validators.compose([Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z0-9 -]*$'), this.sameNameValidator(this.selectedFeature+1)])],
      value: ['', Validators.compose([Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z0-9 -]*$')])],
    });

  }

  getFromFromArrayFeature(index: number):FormGroup {
    if(!this.getFeaturesArray().at(index)){
      return new FormGroup({});
    }
    return this.getFeaturesArray().at(index) as FormGroup;

  }

  closeAddNewFeature(modal: NgbActiveModal) {
    modal.close('Close click');
    this.getFeaturesArray().removeAt(this.selectedFeature);
    this.selectedFeature = this.selectedFeature - 1;
  }

  saveNewFeature(modal: NgbActiveModal) {
    modal.close('Save click');
    this.savedFeatures.push(true);
    this.handler.throwSuccessToast('Caratteristica aggiunta con successo');



  }

  modifyFeature(modal: NgbActiveModal) {
    modal.close('Save click');
    this.savedFeatures[this.selectedFeature] = true;
    this.handler.throwSuccessToast('Caratteristica modificata con successo');


  }

  openModalModifyFeature(i: number, feature: TemplateRef<any>) {
    this.selectedFeature = i;
    const  modal=this.modalService.open(feature);
    this.savedFeatures[i] = false;
    this.modify = true;

    modal.result.then(
      (reason) => {
      },
      (reason) => {
        this.savedFeatures[this.selectedFeature] = true;

      }
    );
  }

  closeModifyFeauture(modal: NgbActiveModal) {
    modal.close('Close click');
    this.savedFeatures[this.selectedFeature] = true;

  }

  deleteFeature(i: number) {
    this.getFeaturesArray().removeAt(i);
    this.savedFeatures.splice(i, 1);
    this.selectedFeature = this.selectedFeature - 1;
    this.handler.throwSuccessToast('Caratteristica eliminata con successo');

  }
  focusName$ = new Subject<string>();
  clickName$ = new Subject<string>();
  @ViewChild('instanceName', { static: true }) instanceName!: NgbTypeahead;
  searchFeatureName: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.clickName$.pipe(
      filter(() => this.instanceName && !this.instanceName.isPopupOpen())
    );    const inputFocus$ = this.focusName$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) => this.filterFeatureNameList(term, this.getFeatureNameList())),
    );
  };

  private getFeatureNameList() {
    return this.features.map((feature) => feature.name);
  }

  private filterFeatureNameList(term: string, featureNameList: string[]) {
    let featureNameListFiltered = featureNameList.filter((feature) => feature.toLowerCase().indexOf(term.toLowerCase()) > -1);

    const featuresArray = this.getFeaturesArray();
    for(let i = 0; i < featuresArray.length; i++) {
      const featureName = this.getFromFromArrayFeature(i).get('name')?.value.toLowerCase();
      if ( this.selectedFeature !== i) {
        featureNameListFiltered = featureNameListFiltered.filter((feature) => feature.toLowerCase() !== featureName);
      }
    }
    return featureNameListFiltered.slice(0, 10); // Ensure we only return the top 10 results
  }

  private sameNameValidator(index:number) {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const name = this.getFromFromArrayFeature(index).get('name')?.value;
      const featuresArray = this.getFeaturesArray();
      for(let i = 0; i < featuresArray.length; i++) {
        const featureName = this.getFromFromArrayFeature(i).get('name')?.value;
        if (this.selectedFeature !== i && featureName === name) {
          this.getFromFromArrayFeature(index).get('name')?.setErrors({ 'sameName': true });
        }
      }
      this.getFromFromArrayFeature(index).get('name')?.setErrors(null);
      return null;
    };}

  focusValue$ = new Subject<string>();
  clickValue$ = new Subject<string>();
  @ViewChild('instanceValue', { static: true }) instanceValue!: NgbTypeahead;
  searchFeatureValue: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.clickValue$.pipe(
      filter(() => this.instanceValue && !this.instanceValue.isPopupOpen())
    );    const inputFocus$ = this.focusValue$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) => this.filterFeatureValueList(term)),
    );
  };

  private filterFeatureValueList(term: string) {
    let currentName = this.getFromFromArrayFeature(this.selectedFeature).get('name')?.value;
    if (!currentName || this.getFeatureNameList().findIndex((name) => name.toLowerCase() === currentName.toLowerCase()) === -1) {
      return [];
    }
    let feature = this.features.find((feature) => feature.name.toLowerCase() === currentName.toLowerCase());
    if (feature) {
      let featureValueList = feature.value;
      let featureValueListFiltered = featureValueList.filter((value) => value.toLowerCase().indexOf(term.toLowerCase()) > -1);
      return featureValueListFiltered.slice(0, 10); // Ensure we only return the top 10 results
    }
    return [];

  }

  saveProductType() {
    this.loading.show();
    const name = this.form.get('name')?.value;
    const language = this.form.get('language')?.value;
    const game = this.form.get('game')?.value;
    const type = this.form.get('type')?.value;
    const photo = this.form.get('photo')?.value;
    const photoUrl = this.form.get('photoUrl')?.value;
    const features = [];
    for(let i=0; i<this.getFeaturesArray().length; i++){
      const featureName = this.getFromFromArrayFeature(i).get('name')?.value;
      const featureValue = this.getFromFromArrayFeature(i).get('value')?.value;
      features.push(new Feature(featureName, featureValue));

    }

    const productType = new ProductTypeRegistration(name, language, game, type, features, photo, photoUrl);
    this.productTypeService.saveProductType(productType).subscribe(
      (response) => {
        this.loading.hide();
        this.activeModal.close('Save click');
      },
      (error) => {
        this.loading.hide();
        if (error.status === 409) {
          this.handler.throwGlobalError('Prodotto già presente');
          return;
        }
        if (error.status == 403) {
          this.handler.throwGlobalError('Non hai i permessi per compiere questa azione').then(() => this.activeModal.close('Save click'));
          return;
        }
        if(error.status == 400){
          this.handler.throwGlobalError('Dati non validi, ricontrolla i campi inseriti');
          return;}

        this.handler.throwGlobalError('Errore durante l\'aggiunta della carta');
        return;

      });

  }
  modifyProductType(){
    this.loading.show();
    const name = this.form.get('name')?.value;
    const language = this.form.get('language')?.value;
    const game = this.form.get('game')?.value;
    const type = this.form.get('type')?.value;
    const photo = this.form.get('photo')?.value;
    const photoUrl = this.form.get('photoUrl')?.value;
    const features = [];
    for(let i=0; i<this.getFeaturesArray().length; i++){
      const featureName = this.getFromFromArrayFeature(i).get('name')?.value;
      const featureValue = this.getFromFromArrayFeature(i).get('value')?.value;
      features.push(new Feature(featureName, featureValue));

    }

    const productTypeRegistration = new ProductTypeRegistration(name, language, game, type, features, photo, photoUrl);
    const productTypeId = this.productType?.id;
    if(!productTypeId){
      this.loading.hide();
      this.handler.throwGlobalError('Errore durante la modifica del prodotto');
      return;}
    this.productTypeService.modifyProductType( productTypeId, productTypeRegistration).subscribe(
      (response) => {
        this.loading.hide();
        this.activeModal.close('Save click');
      },
      (error) => {
        this.loading.hide();
        this.activeModal.close('Save click');
        if (error.status === 404) {
          this.handler.throwGlobalError('Prodotto non trovato');
          return;
        }
        if (error.status == 403) {
          this.handler.throwGlobalError('Non hai i permessi per compiere questa azione').then(() => this.activeModal.close('Save click'));
          return;
        }

        this.handler.throwGlobalError('Errore durante l\'aggiunta della carta');
        return;

      })};






  focusLanguage$ = new Subject<string>();
  clickLanguage$ = new Subject<string>();
  @ViewChild('languageValue', { static: true }) languageValue!: NgbTypeahead;
  searchLanguages: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.clickLanguage$.pipe(
      filter(() => this.languageValue && !this.languageValue.isPopupOpen())
    );
    const inputFocus$ = this.focusLanguage$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) => this.getArray(this.languages)
        .filter((language) => language.toLowerCase().indexOf(term.toLowerCase()) > -1)
        .slice(0, 10) // Ensure we only return the top 10 results
      ),
    );
  };
  focusGame$ = new Subject<string>();
  clickGame$ = new Subject<string>();
  @ViewChild('gameValue', { static: true }) gameValue!: NgbTypeahead;
  searchGames: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.clickGame$.pipe(
      filter(() => this.gameValue && !this.gameValue.isPopupOpen())
    );
    const inputFocus$ = this.focusGame$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) => this.getArray(this.games)
        .filter((game) => game.toLowerCase().indexOf(term.toLowerCase()) > -1)
        .slice(0, 10) // Ensure we only return the top 10 results
      ),
    );
  };
  focusTypes$ = new Subject<string>();
  clickTypes$ = new Subject<string>();
  @ViewChild('typeValue', { static: true }) typeValue!: NgbTypeahead;
  searchTypes: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.clickTypes$.pipe(
      filter(() => this.typeValue && !this.typeValue.isPopupOpen())
    );
    const inputFocus$ = this.focusTypes$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) => this.getArray(this.types)
        .filter((type) => type.toLowerCase().indexOf(term.toLowerCase()) > -1)
        .slice(0, 10) // Ensure we only return the top 10 results
      ),
    );
  };


  private getArray(map: Map<string, string>) {
    return Array.from(Object.values(map));
  }
  private getKeys(map: Map<string, string>) {
    return Array.from(Object.keys(map));
  }

  clearFile() {
    this.form.get('photo')?.setValue(null);
  }
  clearUrl() {
    this.form.get('photoUrl')?.setValue("");
  }
  photoOrUrlIsPresent(): boolean {
    return this.form.get('photo')?.value==null && this.form.get('photoUrl')?.value=="";}








}
