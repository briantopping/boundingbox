import { Component, OnInit } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-home',
  host: {'class': 'd-flex flex-column'},
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  content = '';

  constructor(private http: HttpClient) { }

  ngOnInit() {
    this.handleClick(null)
  }

  handleClick(event: MouseEvent) {
    this.http.get("/api/rects",  {responseType: 'text'}).subscribe((content:string) => this.content = content)
  }
}
