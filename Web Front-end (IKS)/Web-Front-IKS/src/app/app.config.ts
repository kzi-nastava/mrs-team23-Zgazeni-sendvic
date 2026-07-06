import { ApplicationConfig, provideAppInitializer, inject } from '@angular/core';
import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { routes } from './app.routes';
import { authInterceptor } from './services/auth.interceptor';
import { AccountService } from './service/account.service';

export function initAuth() {
  const accountService = inject(AccountService);

  return firstValueFrom(accountService.getMyAccount()).catch(() => null);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),

    provideHttpClient(
      withInterceptors([authInterceptor])
    ),

    provideAppInitializer(initAuth)
  ]
};
