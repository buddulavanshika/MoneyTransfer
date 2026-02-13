import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class App implements OnInit {

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // 🔥 realtime ownership check
    this.authService.claimOwnership();
  }
}
