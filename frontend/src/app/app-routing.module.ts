import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DisplayComponent } from './component/display/display.component';
import { NotFoundComponent } from './component/not-found/not-found.component';
import { StartComponent } from './component/start/start.component';

const routes: Routes = [
  { path: '', component: StartComponent },
  { path: 'error/404', component: NotFoundComponent },
  { path: ':shortUrl', component: DisplayComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
