import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule],
  templateUrl: './admin-layout.html'
})
export class AdminLayoutComponent {
  private auth = inject(AuthService);
  readonly user = this.auth.currentUser;
  logout(): void { this.auth.logout(); }
}
