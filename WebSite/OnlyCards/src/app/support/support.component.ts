import { Component } from '@angular/core';
import {LoginService} from "../../service/login/login.service";

@Component({
  selector: 'app-support',
  templateUrl: './support.component.html',
  styleUrl: './support.component.css'
})

export class SupportComponent {
  constructor(protected login: LoginService) {}
}
