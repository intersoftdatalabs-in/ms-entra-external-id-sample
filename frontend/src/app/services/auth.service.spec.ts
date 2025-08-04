import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
    sessionStorage.clear();
  });

  it('should store tokens on login', () => {
    service.login('test@example.com', 'password').subscribe();
    const req = httpMock.expectOne('http://localhost:8080/login');
    // Create a valid JWT with future expiration
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 1000 }));
    const validToken = `header.${payload}.signature`;
    req.flush({ accessToken: validToken, refreshToken: 'def' });
    expect(localStorage.getItem('accessToken')).toBe(validToken);
    expect(localStorage.getItem('refreshToken')).toBe('def');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('should clear tokens on logout', () => {
    localStorage.setItem('accessToken', 'abc');
    localStorage.setItem('refreshToken', 'def');
    service.logout().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/logout');
    req.flush({});
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
  });

  it('should detect expired token', () => {
    // Create a token with exp in the past
    const expiredPayload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) - 100 }));
    const token = `header.${expiredPayload}.signature`;
    localStorage.setItem('accessToken', token);
    expect(service.isTokenExpired(token)).toBeTrue();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should extract roles from token', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 1000, roles: ['ADMIN', 'USER'] }));
    const token = `header.${payload}.signature`;
    localStorage.setItem('accessToken', token);
    expect(service.getUserRoles()).toEqual(['ADMIN', 'USER']);
  });

  it('should refresh token', () => {
    localStorage.setItem('refreshToken', 'refresh123');
    service.refreshToken().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/refresh');
    req.flush({ accessToken: 'newtoken' });
    expect(localStorage.getItem('accessToken')).toBe('newtoken');
  });

  afterEach(() => {
    httpMock.verify();
  });
});
